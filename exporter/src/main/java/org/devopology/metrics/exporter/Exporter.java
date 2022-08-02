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
import io.prometheus.client.Info;
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
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HttpHandler;
import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;
import org.devopology.common.password.ObfuscatedPassword;
import org.devopology.common.precondition.Precondition;
import org.devopology.metrics.exporter.collector.CollectorWrapper;
import org.devopology.metrics.exporter.resources.Resources;
import org.devopology.metrics.exporter.template.Template;
import org.devopology.metrics.exporter.undertow.handler.BasicAuthenticationHttpHandler;
import org.devopology.metrics.exporter.undertow.handler.DispatcherHttpHandler;
import org.devopology.metrics.exporter.undertow.handler.FaviconHttpHandler;
import org.devopology.metrics.exporter.undertow.handler.HealthyHttpHandler;
import org.devopology.metrics.exporter.undertow.handler.StaticContentHttpHandler;
import org.devopology.metrics.exporter.undertow.handler.predicate.RequestPathExact;
import org.devopology.metrics.exporter.undertow.handler.MetricsHttpHandler;
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
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to expose JMX and Prometheus metrics via a web server
 */
@SuppressWarnings("unchecked")
public class Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Exporter.class);;
    private static final String CRLF = "\r\n";

    private static final String METRICS_EXPORTER_VERSION = "metrics_exporter_version";
    private static final String METRICS_EXPORTER_VERSION_HELP = "metrics-exporter version";
    private static final String METRICS_EXPORTER_MODE = "metrics_exporter_mode";
    private static final String METRICS_EXPORTER_MODE_HELP = "metrics-exporter mode";

    private static final String VERSION = "version";

    enum Mode { STANDALONE, AGENT }

    private Mode mode;
    private Resources resources;
    private CollectorRegistry collectorRegistry;
    private List<Collector> collectorList;
    private Configuration configuration;
    private CustomUndertow undertow;

    /**
     * Constructor
     */
    public Exporter() {
        resources = new Resources();
        collectorRegistry = CollectorRegistry.defaultRegistry;
        collectorList = new ArrayList<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (undertow != null) {
                try {
                    undertow.stop();
                } catch (Exception e) {
                    // DO NOTHING
                }
            }
        }));
    }

    /**
     * Method to start the Exporter (called via reflection)
     *
     * @param yamlConfigurationFile
     * @throws Exception
     */
    public synchronized void start(File yamlConfigurationFile) throws Exception {
        Precondition.notNull(yamlConfigurationFile, "argument is null");

        cleanup();

        System.setProperty("org.jboss.logging.provider", "slf4j");

        try {
            String version = Version.getVersion();
            LOGGER.info(String.format("%s", version));

            // Set up metrics exporter version info metric
            Info metricsExporterVersionInfo =
                    Info.build().name(METRICS_EXPORTER_VERSION).help(METRICS_EXPORTER_VERSION_HELP).register();

            metricsExporterVersionInfo.info("version", version);

            // Set up metrics exporter version mode metric
            Info metricsExporterModeInfo =
                    Info.build().name(METRICS_EXPORTER_MODE).help(METRICS_EXPORTER_MODE_HELP).register();

            if ("standalone".equals(System.getProperty("exporter.mode"))) {
                mode = Mode.STANDALONE;
                metricsExporterModeInfo.info("mode", "standalone");
            } else {
                mode = Mode.AGENT;
                metricsExporterModeInfo.info("mode", "agent");
            }

            // Create and load configuration
            configuration = new Configuration();
            configuration.load(yamlConfigurationFile);

            // Configure exports
            configureExports();

            String serverHost = configuration.getString(ConfigurationPath.EXPORTER_SERVER_HOST_PATH);
            Integer serverPort = configuration.getInteger(ConfigurationPath.EXPORTER_SERVER_PORT_PATH);

            if ((serverPort < 1) || (serverPort > 65535)) {
                throw new ConfigurationException(String.format("server port must be in the range %d - %d (inclusive)", 1, 65535));
            }

            LOGGER.info(String.format("Undertow host [%s]", serverHost));
            LOGGER.info(String.format("Undertow port [%s]", serverPort));

            // Create the Undertow builder
            CustomUndertow.Builder undertowBuilder = CustomUndertow.builder();
            undertowBuilder.setServerOption(UndertowOptions.ENABLE_HTTP2, true);

            Integer ioThreads = configuration.getInteger(ConfigurationPath.EXPORTER_SERVER_THREADS_IO_PATH, false);
            if ((ioThreads != null) && (ioThreads < 1)) {
                throw new ConfigurationException(String.format("io threads must be greater >= %d", 1));
            }

            if (ioThreads != null) {
                LOGGER.info(String.format("Undertow IO threads [%d]" , ioThreads));
            }

            Integer workerThreads = configuration.getInteger(ConfigurationPath.EXPORTER_SERVER_THREADS_WORKER_PATH, false);
            if ((workerThreads != null) && (workerThreads < 1)) {
                throw new ConfigurationException(String.format("worked threads must be greater >= %d", 1));
            }

            if (workerThreads != null) {
                LOGGER.info(String.format("Undertow worker threads [%d]", workerThreads));
            }

            // TODO if server host is a valid domain name, but unknown Undertow will throw an exception

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

            // Set up the HttpHandler handling
            DispatcherHttpHandler dispatcherHttpHandler = new DispatcherHttpHandler();

            // Set up the HttpHandler for "/favicon.ico" (404 NOT FOUND)
            dispatcherHttpHandler.addHttpHandler(new RequestPathExact("/favicon.ico"), new FaviconHttpHandler());

            // Set up the HttpHandler for "/-/healthy" and "/-/health"
            dispatcherHttpHandler.addHttpHandler(
                    new RequestPathExact("/-/healthy").or(new RequestPathExact("/-/health")), new HealthyHttpHandler());

            // Set up the HttpHandler for "/-/information"
            Template template = new Template(
                    resources.getResourceAsString("/information..html", StandardCharsets.UTF_8));

            Map<String, String> values = new HashMap<>();
            values.put("version", Version.getVersion());

            dispatcherHttpHandler.addHttpHandler(
                    new RequestPathExact("/-/information"),
                    new StaticContentHttpHandler(200, "text/html", template.merge(values)));

            // Set up the default HttpHandler (metrics output)
            MetricsHttpHandler metricsHttpHandler = new MetricsHttpHandler(isCachingEnabled, cacheMilliseconds);
            dispatcherHttpHandler.setDefaultHttpHandler(metricsHttpHandler);

            HttpHandler httpHandler = dispatcherHttpHandler;

            Boolean isBasicAuthenticationEnabled = configuration.getBoolean(ConfigurationPath.EXPORTER_SERVER_AUTHENTICATION_BASIC_ENABLED_PATH);
            LOGGER.info(String.format("Undertow BASIC authentication enabled [%b]", isBasicAuthenticationEnabled));
            if (isBasicAuthenticationEnabled) {
                String basicAuthenticationUsername = configuration.getString(ConfigurationPath.EXPORTER_SERVER_AUTHENTICATION_BASIC_USERNAME_PATH);
                String basicAuthenticationSaltedPassword = configuration.getString(ConfigurationPath.EXPORTER_SERVER_AUTHENTICATION_BASIC_PASSWORD_PATH);

                IdentityManager identityManager =
                        new UsernameSaltedPasswordIdentityManager(
                            basicAuthenticationUsername, basicAuthenticationSaltedPassword);

                httpHandler = new BasicAuthenticationHttpHandler(identityManager, dispatcherHttpHandler);
            }

            if (isCachingEnabled) {
                LOGGER.info(String.format("caching enabled [%b]", isCachingEnabled));
            }

            // Set the Undertow HttpHandler
            undertowBuilder.setHandler(httpHandler);

            LOGGER.info("Undertow starting");

            // Create the Xnio instance directly due to a ClassNotFoundException
            // in the core Undertow implementation for our use case
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

            // Create the XnioWorker instance
            XnioWorker xnioWorker = xnio.createWorker(xnioWorkerOptionMapBuilder.getMap());
            undertowBuilder.setWorker(xnioWorker);

            // Build and start the Undertow instance
            undertow = undertowBuilder.build();
            undertow.start();

            LOGGER.info("Undertow running");
            LOGGER.info("running");
        } catch (Exception e) {
            LOGGER.error("Undertow stopping");

            if (undertow != null) {
                try {
                    undertow.stop();
                } catch (Exception e2) {
                    // DO NOTHING
                }
            }

            LOGGER.info("Undertow stopped");

            throw e;
        }
    }

    /**
     * Method to stop the Exporter (called via reflection)
     */
    public synchronized void stop() {
        LOGGER.info("stopping");
        cleanup();
        LOGGER.info("stopped");
    }

    /**
     * Method to configure exports
     *
     * @throws Exception
     */
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
            JmxCollector.Mode jmxCollectorMode = JmxCollector.Mode.AGENT;
            if (mode == Mode.STANDALONE) {
                jmxCollectorMode = JmxCollector.Mode.STANDALONE;
            }

            // Create the JMXExporter
            Collector collector = new JmxCollector(configuration.getYamlConfigurationFile(), jmxCollectorMode);

            /**
             * Handle "startDelaySeconds" as a special case.
             * <p>
             * Collection of ALL metrics fails if "startDelaySeconds" is configuration in the JmxExporter.
             * <p></p>
             * In this scenario, we create a wrapper class to handle the JmxExporter behavior,
             * returning other metrics before the JMX exporter is ready
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

    /**
     * Method to clean up resources
     */
    private void cleanup() {
        // Stop the Undertow instance
        if (undertow != null) {
            try {
                undertow.stop();
            } catch (Throwable t) {
                // DO NOTHING
            }

            undertow = null;
        }

        // Remove all collectors that we registered
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
}
