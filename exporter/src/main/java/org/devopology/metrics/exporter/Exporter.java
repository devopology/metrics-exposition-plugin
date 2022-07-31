/*
 * Copyright 2022 Douglas Hoard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devopology.metrics.exporter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.BufferPoolsExports;
import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.CompilationExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryAllocationExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.ThreadExports;
import io.prometheus.client.hotspot.VersionInfoExports;
import io.prometheus.jmx.JmxCollector;
import io.undertow.CustomUndertow;
import io.undertow.UndertowOptions;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.server.HttpHandler;
import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;
import org.devopology.common.password.ObfuscatedPassword;
import org.devopology.common.precondition.Precondition;
import org.devopology.metrics.exporter.collector.CollectorWrapper;
import org.devopology.metrics.exporter.undertow.handler.DispatchingHttpHandler;
import org.devopology.metrics.exporter.undertow.security.UsernameSaltedPasswordIdentityManager;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;
import org.xnio.nio.NioXnioProvider;
import org.yaml.snakeyaml.Yaml;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Class to expose JMX and Prometheus metrics via a web server
 */
@SuppressWarnings("unchecked")
public class Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Exporter.class);;

    private Configuration configuration;
    private CollectorRegistry collectorRegistry;
    private List<Collector> collectorList;
    private CustomUndertow undertow;

    /**
     * Constructor
     */
    public Exporter() {
        collectorRegistry = CollectorRegistry.defaultRegistry;
        collectorList = new ArrayList<>();
    }

    /**
     * Method to start (called via reflection)
     *
     * @param yamlConfigurationFile
     * @throws Exception
     */
    public synchronized void start(File yamlConfigurationFile) throws Exception {
        Precondition.notNull(yamlConfigurationFile, "argument is null");

        cleanup();

        System.setProperty("org.jboss.logging.provider", "slf4j");

        try {
            LOGGER.info(String.format("%s", Version.getVersion()));

            configuration = new Configuration();
            configuration.load(yamlConfigurationFile);

            configureExports();

            String serverHost = configuration.getString(ConfigurationPath.EXPORTER_SERVER_HOST_PATH);
            Integer serverPort = configuration.getInteger(ConfigurationPath.EXPORTER_SERVER_PORT_PATH);

            if ((serverPort < 1) || (serverPort > 65535)) {
                throw new ConfigurationException(String.format("server port must be in the range %d - %d (inclusive)", 1, 65535));
            }

            LOGGER.info(String.format("Undertow host [%s]", serverHost));
            LOGGER.info(String.format("Undertow port [%s]", serverPort));

            CustomUndertow.Builder undertowBuilder = CustomUndertow.builder();
            undertowBuilder.setServerOption(UndertowOptions.ENABLE_HTTP2, true);

            Integer ioThreads = configuration.getInteger(ConfigurationPath.EXPORTER_SERVER_THREADS_IO_PATH, false);
            if (ioThreads != null) {
                if (ioThreads < 1) {
                    throw new ConfigurationException(String.format("io threads must be greater >= %d", 1));
                }

                LOGGER.info(String.format("Undertow IO threads [%d]" , ioThreads));
            }

            Integer workerThreads = configuration.getInteger(ConfigurationPath.EXPORTER_SERVER_THREADS_WORKER_PATH, false);
            if (workerThreads != null) {
                if (workerThreads < 1) {
                    throw new ConfigurationException(String.format("worked threads must be greater >= %d", 1));
                }

                LOGGER.info(String.format("Undertow worker threads [%d]" , workerThreads));
            }

            // TODO if server host is a validate domain name, but unknown Undertow will throw an exception

            Boolean isSSLEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_SSL_ENABLED_PATH);
            LOGGER.info(String.format("Undertow SSL/TLS enabled [%b]", isSSLEnabled));

            if (isSSLEnabled) {
                File keyStore = configuration.getReadableFile(ConfigurationPath.EXPORTER_SERVER_SSL_KEYSTORE_FILENAME_PATH);
                String keyStoreType = configuration.getString(ConfigurationPath.EXPORTER_SERVER_SSL_KEYSTORE_TYPE_PATH);
                String keyStorePassword = configuration.getString(ConfigurationPath.EXPORTER_SERVER_SSL_KEYSTORE_PASSWORD_PATH);

                ObfuscatedPassword obfuscatedPassword = new ObfuscatedPassword(keyStorePassword);
                keyStorePassword = obfuscatedPassword.getUnobfuscatedPassword();

                String certificateAlias = configuration.getString(ConfigurationPath.EXPORTER_SERVER_SSL_CERTIFICATE_ALIAS_PATH);
                String sslProtocol = configuration.getString(ConfigurationPath.EXPORTER_SERVER_SSL_PROTOCOL_PATH);

                SSLContext sslContext = createSSLContext(certificateAlias, keyStore, keyStoreType, keyStorePassword, sslProtocol);
                undertowBuilder.addHttpsListener(serverPort, serverHost, sslContext);
            } else {
                undertowBuilder.addHttpListener(serverPort, serverHost);
            }

            Long cacheMilliseconds = null;
            Boolean isCachingEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_CACHING_ENABLED_PATH);
            if (isCachingEnabled) {
                cacheMilliseconds = configuration.getLong(ConfigurationPath.EXPORTER_SERVER_CACHING_MILLISECONDS_PATH);
            }

            HttpHandler httpHandler = new DispatchingHttpHandler(isCachingEnabled, cacheMilliseconds);

            Boolean isBasicAuthenticationEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_AUTHENTICATION_BASIC_ENABLED_PATH);
            LOGGER.info(String.format("Undertow BASIC authentication enabled [%b]", isBasicAuthenticationEnabled));
            if (isBasicAuthenticationEnabled) {
                String basicAuthenticationUsername = configuration.getString(ConfigurationPath.EXPORTER_SERVER_AUTHENTICATION_BASIC_USERNAME_PATH);
                String basicAuthenticationSaltedPassword = configuration.getString(ConfigurationPath.EXPORTER_SERVER_AUTHENTICATION_BASIC_PASSWORD_PATH);

                IdentityManager identityManager =
                        new UsernameSaltedPasswordIdentityManager(
                            basicAuthenticationUsername, basicAuthenticationSaltedPassword);

                httpHandler = addSecurity(httpHandler, identityManager);
            }

            if (isCachingEnabled) {
                LOGGER.info(String.format("caching enabled [%b]", isCachingEnabled));
            }

            undertowBuilder.setHandler(httpHandler);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (undertow != null) {
                    try {
                        undertow.stop();
                    } catch (Exception e) {
                        // DO NOTHING
                    }
                }
            }));

            LOGGER.info("Undertow starting");

            // Create the Xnio class directly due to issue
            // ClassNotfoundExceptions in the core Undertow
            // implementation for our use case
            Xnio xnio = new NioXnioProvider().getInstance();
            undertowBuilder.setXnio(xnio);

            // Create the XnioWorker options
            OptionMap.Builder xnioWorkerOptionMapBuilder = OptionMap.builder();
            xnioWorkerOptionMapBuilder
                    .set(Options.CONNECTION_HIGH_WATER, 100)
                    .set(Options.CONNECTION_LOW_WATER, 100)
                    .set(Options.TCP_NODELAY, true)
                    .set(Options.CORK, true);

            if (workerThreads != null) {
                xnioWorkerOptionMapBuilder.set(Options.WORKER_IO_THREADS, workerThreads);
            }

            if (ioThreads != null) {
                xnioWorkerOptionMapBuilder.set(Options.WORKER_TASK_CORE_THREADS, ioThreads);
                xnioWorkerOptionMapBuilder.set(Options.WORKER_TASK_MAX_THREADS, ioThreads);
            }

            // Create the XnioWorker directly
            XnioWorker xnioWorker = xnio.createWorker(xnioWorkerOptionMapBuilder.getMap());
            undertowBuilder.setWorker(xnioWorker);

            undertow = undertowBuilder.build();

            undertow.start();

            LOGGER.info("Undertow running");
            LOGGER.info("running");
        } catch (Exception e) {
            if (undertow != null) {
                try {
                    undertow.stop();
                } catch (Exception e2) {
                    // DO NOTHING
                }
            }

            throw e;
        }
    }

    /**
     * Method to stop (called via reflection)
     */
    public synchronized void stop() {
        LOGGER.info("stopping");

        cleanup();

        LOGGER.info("stopped");
    }

    private void configureExports() throws Exception {
        Boolean isHotSpotBufferPoolsExportsEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_EXPORTS_HOTSPOT_BUFFER_POOLS_ENABLED_PATH);
        LOGGER.info(String.format("HotSpot buffer-pools exports enabled [%b]", isHotSpotBufferPoolsExportsEnabled));
        if (isHotSpotBufferPoolsExportsEnabled) {
            collectorList.add(new BufferPoolsExports().register());
        }

        Boolean isHotSpotClassLoadingExportsEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_EXPORTS_HOTSPOT_CLASS_LOADING_ENABLED_PATH);
        LOGGER.info(String.format("HotSpot class loading exports enabled [%b]", isHotSpotClassLoadingExportsEnabled));
        if (isHotSpotClassLoadingExportsEnabled) {
            collectorList.add(new ClassLoadingExports().register());
        }

        Boolean isHotSpotCompilationExportsEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_EXPORTS_HOTSPOT_COMPILATION_ENABLED_PATH);
        LOGGER.info(String.format("HotSpot compilation exports enabled [%b]", isHotSpotClassLoadingExportsEnabled));
        if (isHotSpotCompilationExportsEnabled) {
            collectorList.add(new CompilationExports().register());
        }

        Boolean isHotSpotGarbageCollectorExportsEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_EXPORTS_HOTSPOT_GARBAGE_COLLECTOR_ENABLED_PATH);
        LOGGER.info(String.format("HotSpot garbage-collector exports enable [%b]", isHotSpotGarbageCollectorExportsEnabled));
        if (isHotSpotGarbageCollectorExportsEnabled) {
            collectorList.add(new GarbageCollectorExports().register());
        }

        Boolean isHotSpotMemoryAllocationExportsEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_EXPORTS_HOTSPOT_MEMORY_ALLOCATION_ENABLED_PATH);
        LOGGER.info(String.format("HotSpot memory-allocation exports enabled [%b]", isHotSpotMemoryAllocationExportsEnabled));
        if (isHotSpotMemoryAllocationExportsEnabled) {
            collectorList.add(new MemoryAllocationExports().register());
        }

        Boolean isHotSpotMemoryPoolsExportsEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_EXPORTS_HOTSPOT_MEMORY_POOLS_ENABLED_PATH);
        LOGGER.info(String.format("HotSpot memory-pools exports enabled [%b]", isHotSpotMemoryPoolsExportsEnabled));
        if (isHotSpotMemoryPoolsExportsEnabled) {
            collectorList.add(new MemoryPoolsExports().register());
        }

        Boolean isHotSpotThreadExportsEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_EXPORTS_HOTSPOT_THREAD_ENABLED_PATH);
        LOGGER.info(String.format("HotSpot thread exports enabled [%s]", isHotSpotThreadExportsEnabled));
        if (isHotSpotThreadExportsEnabled) {
            collectorList.add(new ThreadExports().register());
        }

        Boolean isHotSpotVersionInfoExportsEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_EXPORTS_HOTSPOT_VERSION_INFO_ENABLED_PATH);
        LOGGER.info(String.format("HotSpot version info exports enabled [%b]", isHotSpotVersionInfoExportsEnabled));
        if (isHotSpotVersionInfoExportsEnabled) {
            collectorList.add(new VersionInfoExports().register());
        }

        Boolean isHotSpotStandardExportsEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_EXPORTS_HOTSPOT_STANDARD_ENABLED_PATH);
        LOGGER.info(String.format("HotSpot standard exports enabled [%b]", isHotSpotStandardExportsEnabled));
        if (isHotSpotStandardExportsEnabled) {
            collectorList.add(new StandardExports().register());
        }

        Boolean isJMXExportsEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_EXPORTS_JMX_ENABLED_PATH);
        LOGGER.info(String.format("JMX exports enabled [%b]", isJMXExportsEnabled));
        if (isJMXExportsEnabled) {
            Collector collector = new JmxCollector(configuration.getYamlConfigurationFile(), JmxCollector.Mode.AGENT);

            /**
             * Handle "startDelaySeconds" as a special case.
             * <p>
             * Collection of ALL metrics fails if "startDelaySeconds" is configuration in the JmxExporter.
             * <p></p>
             * In this scenario, we create a wrapper class to handle the JmxExporter behavior.
             */
            int startDelaySeconds = 0;
            Map<String, ?> yamlMap2 = new Yaml().load(new FileReader(configuration.getYamlConfigurationFile()));
            if (yamlMap2.containsKey("startDelaySeconds")) {
                try {
                    startDelaySeconds = (Integer) yamlMap2.get("startDelaySeconds");
                } catch (ClassCastException e) {
                    // DO NOTHING
                }
            }

            if (startDelaySeconds > 0) {
                collector = new CollectorWrapper(collector);
            }

            collectorList.add(collector.register());
        }
    }

    private void cleanup() {
        if (undertow != null) {
            try {
                undertow.stop();
            } catch (Throwable t) {
                // DO NOTHING
            }

            undertow = null;
        }

        if (collectorList != null) {
            for (Collector collector : collectorList) {
                collectorRegistry.unregister(collector);
            }

            collectorList.clear();
        }
    }

    /**
     * Method to create an SSLContext
     *
     * @param certificateAlias
     * @param keyStoreFile
     * @param keyStoreType
     * @param keyStorePassword
     * @param sslProtocol
     * @return
     * @throws Exception
     */
    private SSLContext createSSLContext(String certificateAlias, File keyStoreFile, String keyStoreType, String keyStorePassword, String sslProtocol) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());

        boolean foundCertificate = false;
        Enumeration<String> keyStoreAliases = keyStore.aliases();
        while (keyStoreAliases.hasMoreElements()) {
            String keyStoreAlias = keyStoreAliases.nextElement();
            if (keyStoreAlias.equals(certificateAlias)) {
                foundCertificate = true;
                break;
            }
        }

        if (!foundCertificate) {
            throw new IllegalArgumentException("certificate alias [" + certificateAlias + "] doesn't exist in keystore [" + keyStore + "]");
        }

        KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());

        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

        SSLContext sslContext = SSLContext.getInstance(sslProtocol);
        sslContext.init(
                keyManagerFactory.getKeyManagers(),
                null,
                new SecureRandom());

        return sslContext;
    }

    /**
     * Method to add security to an HttpHandler
     *
     * @param handler
     * @param identityManager
     * @return
     */
    private static HttpHandler addSecurity(HttpHandler handler, IdentityManager identityManager) {
        handler = new AuthenticationCallHandler(handler);
        handler = new AuthenticationConstraintHandler(handler);
        List<AuthenticationMechanism> mechanisms = Collections.<AuthenticationMechanism>singletonList(new BasicAuthenticationMechanism("/"));
        handler = new AuthenticationMechanismsHandler(handler, mechanisms);
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, handler);
        return handler;
    }
}
