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
import io.undertow.Undertow;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.server.HttpHandler;
import org.devopology.common.precondition.Precondition;
import org.devopology.metrics.exporter.web.server.handler.DispatchingHttpHandler;
import org.devopology.metrics.exporter.web.server.security.UsernameSaltedPasswordIdentityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Class to expose JMX and Prometheus metrics via a web server
 */
@SuppressWarnings("unchecked")
public class MetricsExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsExporter.class);

    private Undertow undertow;

    /**
     * Method to execute the plugin (called via reflection)
     *
     * @param argument
     * @throws Exception
     */
    public void start(String argument) throws Exception {
        Precondition.notNull(argument, "argument is null");
        Precondition.notEmpty(argument, "argument is empty");

        System.setProperty("org.jboss.logging.provider", "slf4j");

        try {
            LOGGER.info("version " + Version.getVersion());
            LOGGER.info("starting");

            File metricsExporterYaml = new File(argument);

            Precondition.exists(metricsExporterYaml, String.format("file [%s] doesn't exist", argument));
            Precondition.isFile(metricsExporterYaml,  String.format("file [%s] isn't a file", argument));
            Precondition.canRead(metricsExporterYaml, String.format("file [%s] isn't readable", argument));

            LOGGER.info("loading configuration");

            Configuration configuration = new Configuration();
            configuration.load(new FileReader(metricsExporterYaml));

            String serverHost = configuration.getString(Constant.EXPORTER_SERVER_HOST_PATH);
            Integer serverPort = configuration.getInteger(Constant.EXPORTER_SERVER_PORT_PATH);

            if ((serverPort < 1) || (serverPort > 65535)) {
                throw new ConfigurationException(String.format("server port must be in the range %d - %d (inclusive)", 1, 65535));
            }

            Undertow.Builder undertowBuilder = Undertow.builder();

            // TODO if server host is a validate domain name, but unknown Undertow will throw an exception

            Boolean isSSLEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_SSL_ENABLED_PATH);
            if (isSSLEnabled) {
                LOGGER.info("SSL/TLS enabled");
                File keyStore = configuration.getReadableFile(Constant.EXPORTER_SERVER_SSL_KEYSTORE_FILENAME_PATH);
                String keyStoreType = configuration.getString(Constant.EXPORTER_SERVER_SSL_KEYSTORE_TYPE_PATH);
                String keyStorePassword = configuration.getString(Constant.EXPORTER_SERVER_SSL_KEYSTORE_PASSWORD_PATH);
                String certificateAlias = configuration.getString(Constant.EXPORTER_SERVER_SSL_CERTIFICATE_ALIAS_PATH);
                String sslProtocol = configuration.getString(Constant.EXPORTER_SERVER_SSL_PROTOCOL_PATH);

                SSLContext sslContext = createSSLContext(certificateAlias, keyStore, keyStoreType, keyStorePassword, sslProtocol);
                undertowBuilder.addHttpsListener(serverPort, serverHost, sslContext);
            } else {
                undertowBuilder.addHttpListener(serverPort, serverHost);
            }

            Boolean isCachingEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_CACHING_ENABLED_PATH);
            Long cacheMilliseconds = null;

            if (isCachingEnabled) {
                LOGGER.info("caching enabled");
                cacheMilliseconds = configuration.getLong(Constant.EXPORTER_SERVER_CACHING_MILLISECONDS_PATH);
            }

            HttpHandler httpHandler = new DispatchingHttpHandler(isCachingEnabled, cacheMilliseconds);

            Boolean isBasicAuthenticationEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_AUTHENTICATION_BASIC_ENABLED_PATH);
            if (isBasicAuthenticationEnabled) {
                LOGGER.info("BASIC authentication enabled");

                String basicAuthenticationUsername = configuration.getString(Constant.EXPORTER_SERVER_AUTHENTICATION_BASIC_USERNAME_PATH);
                String basicAuthenticationSaltedPassword = configuration.getString(Constant.EXPORTER_SERVER_AUTHENTICATION_BASIC_PASSWORD_PATH);

                IdentityManager identityManager =
                        new UsernameSaltedPasswordIdentityManager(
                            basicAuthenticationUsername, basicAuthenticationSaltedPassword);

                httpHandler = addSecurity(httpHandler, identityManager);
            }

            undertowBuilder.setHandler(httpHandler);

            Integer ioThreads = configuration.getInteger(Constant.EXPORTER_SERVER_THREADS_IO_PATH, false);
            if (ioThreads != null) {
                if (ioThreads < 1) {
                    throw new ConfigurationException(String.format("io threads must be greater >= %d", 1));
                }

                LOGGER.info("IO threads = " + ioThreads);
                undertowBuilder.setIoThreads(ioThreads);
            }

            Integer workerThreads = configuration.getInteger(Constant.EXPORTER_SERVER_THREADS_WORKER_PATH, false);
            if (workerThreads != null) {
                if (workerThreads < 1) {
                    throw new ConfigurationException(String.format("worked threads must be greater >= %d", 1));
                }

                LOGGER.info("worker threads = " + workerThreads);
                undertowBuilder.setWorkerThreads(workerThreads);
            }

            Boolean isHotSpotBufferPoolsExportsEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_EXPORTS_HOTSPOT_BUFFER_POOLS_ENABLED_PATH);
            if (isHotSpotBufferPoolsExportsEnabled) {
                LOGGER.info("HotSpot buffer-pools exports enabled");
                new BufferPoolsExports().register();
            }

            Boolean isHotSpotClassLoadingExportsEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_EXPORTS_HOTSPOT_CLASS_LOADING_ENABLED_PATH);
            if (isHotSpotClassLoadingExportsEnabled) {
                LOGGER.info("HotSpot class loading exports enabled");
                new ClassLoadingExports().register();
            }

            Boolean isHotSpotCompilationExportsEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_EXPORTS_HOTSPOT_COMPILATION_ENABLED_PATH);
            if (isHotSpotCompilationExportsEnabled) {
                LOGGER.info("HotSpot compilation exports enabled");
                new CompilationExports().register();
            }

            Boolean isHotSpotGarbageCollectorExportsEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_EXPORTS_HOTSPOT_GARBAGE_COLLECTOR_ENABLED_PATH);
            if (isHotSpotGarbageCollectorExportsEnabled) {
                LOGGER.info("HotSpot garbage-collector exports enabled");
                new GarbageCollectorExports().register();
            }

            Boolean isHotSpotMemoryAllocationExportsEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_EXPORTS_HOTSPOT_MEMORY_ALLOCATION_ENABLED_PATH);
            if (isHotSpotMemoryAllocationExportsEnabled) {
                LOGGER.info("HotSpot memory-allocation exports enabled");
                new MemoryAllocationExports().register();
            }

            Boolean isHotSpotMemoryPoolsExportsEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_EXPORTS_HOTSPOT_MEMORY_POOLS_ENABLED_PATH);
            if (isHotSpotMemoryPoolsExportsEnabled) {
                LOGGER.info("HotSpot memory-pools exports enabled");
                new MemoryPoolsExports().register();
            }

            Boolean isHotSpotStandardExportsEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_EXPORTS_HOTSPOT_STANDARD_ENABLED_PATH);
            if (isHotSpotStandardExportsEnabled) {
                LOGGER.info("HotSpot standard exports enabled");
                new StandardExports().register();
            }

            Boolean isHotSpotThreadExportsEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_EXPORTS_HOTSPOT_THREAD_ENABLED_PATH);
            if (isHotSpotThreadExportsEnabled) {
                LOGGER.info("HotSpot thread exports enabled");
                new ThreadExports().register();
            }

            Boolean isHotSpotVersionInfoExportsEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_EXPORTS_HOTSPOT_VERSION_INFO_ENABLED_PATH);
            if (isHotSpotVersionInfoExportsEnabled) {
                LOGGER.info("HotSpot version info exports enabled");
                new VersionInfoExports().register();
            }

            Boolean isJMXExportsEnabled = configuration.getBoolean(Constant.EXPORTER_SERVER_EXPORTS_JMX_ENABLED_PATH);
            if (isJMXExportsEnabled) {
                LOGGER.info("JMX exports enabled");

                File jmxExporterYaml = configuration.getReadableFile(Constant.EXPORTER_SERVER_EXPORTS_JMX_FILENAME_PATH);
                Collector collector = new JmxCollector(jmxExporterYaml, JmxCollector.Mode.AGENT);

                /**
                 * Handle "startDelaySeconds" as a special case.
                 * <p>
                 * Collection of ALL metrics fails if "startDelaySeconds" is configuration in the JmxExporter.
                 * <p></p>
                 * In this scenario, we create a wrapper class to handle the JmxExporter behavior.
                 */
                int startDelaySeconds = 0;
                Map<String, ?> yamlMap2 = new Yaml().load(new FileReader(jmxExporterYaml));
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

                collector.register();
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (undertow != null) {
                    try {
                        undertow.stop();
                    } catch (Exception e) {
                        // DO NOTHING
                    }
                }
            }));

            this.undertow = undertowBuilder.build();

            LOGGER.info("starting web server");

            this.undertow.start();

            LOGGER.info("web server running");
            LOGGER.info("running");
        } catch (ConfigurationException e) {
            LOGGER.error("configuration error");
            LOGGER.error(e.getMessage());

            if (this.undertow != null) {
                try {
                    this.undertow.stop();
                } catch (Throwable t) {
                    // DO NOTHING
                }
            }

            LOGGER.info("exiting");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("generic error");
            LOGGER.error(e.getMessage());

            if (this.undertow != null) {
                try {
                    this.undertow.stop();
                } catch (Throwable t) {
                    // DO NOTHING
                }
            }

            LOGGER.info("exiting");
            System.exit(1);
        }
    }

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
