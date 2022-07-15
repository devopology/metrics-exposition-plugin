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

package org.devopology.metrics.exposition.plugin;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;
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
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to expose JMX and Prometheus metrics via a web server
 */
public class MetricsExpositionPlugin {

    private static final Logger LOGGER = new Logger("[metrics-exposition-plugin]");

    private static final String DEFAULT_SERVER_HOST = "0.0.0.0";
    private static final String DEFAULT_SERVER_SSL_KEYSTORE_TYPE = "PKCS12";
    private static final String DEFAULT_SERVER_SSL_PROTOCOL = "TLSv1.3";

    private Undertow undertow;

    /**
     * Method to execute the plugin (called via reflection)
     *
     * @param map
     * @throws Exception
     */
    public void start(Map<String, String> map) throws Exception {
        try {
            LOGGER.info("starting...");

            Configuration configuration = new Configuration();
            configuration.load(map);
            configuration.putIfAbsent(ConfigurationKey.SERVER_HOST, DEFAULT_SERVER_HOST);

            LOGGER.info("configuration properties:");
            Set<Map.Entry<String, String>> entrySet = configuration.asMap().entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                if (entry.getKey().endsWith(".password")) {
                    LOGGER.info("  [" + entry.getKey() + "] = [******] (masked)");
                } else {
                    LOGGER.info("  [" + entry.getKey() + "] = [" + entry.getValue() + "]");
                }
            }

            LOGGER.info("validating configuration...");

            try {
                validate(configuration);
            } catch (IllegalArgumentException e) {
                LOGGER.error("invalid configuration");
                throw e;
            }

            File prometheusYamlFile = new File(configuration.getString(ConfigurationKey.PROMETHEUS_YAML));
            validate(ConfigurationKey.PROMETHEUS_YAML, prometheusYamlFile);

            Boolean defaultExportsEnabled = configuration.getBoolean(ConfigurationKey.DEFAULT_EXPORTERS_ENABLED, Boolean.TRUE);
            if (defaultExportsEnabled) {
                LOGGER.info("registering default exports...");
                DefaultExports.initialize();
            }

            LOGGER.info("registering JMX collector...");
            new JmxCollector(prometheusYamlFile, JmxCollector.Mode.AGENT).register();

            LOGGER.info("creating web server...");

            Undertow.Builder undertowBuilder = Undertow.builder();

            if (configuration.getBoolean(ConfigurationKey.SERVER_SSL_ENABLED)) {
                LOGGER.info("initializing web server SSL/TLS...");

                SSLContext sslContext = createSSLContext(configuration);
                undertowBuilder.addHttpsListener(
                        configuration.getInteger(ConfigurationKey.SERVER_PORT),
                        configuration.getString(ConfigurationKey.SERVER_HOST),
                        sslContext);
            } else {
                undertowBuilder.addHttpListener(
                        configuration.getInteger(ConfigurationKey.SERVER_PORT),
                        configuration.getString(ConfigurationKey.SERVER_HOST));
            }

            HttpHandler httpHandler = new DispatchingHttpHandler();

            if (configuration.getBoolean(ConfigurationKey.SERVER_AUTHENTICATION_BASIC_ENABLED)) {
                LOGGER.info("initializing web server basic authentication...");

                Map<String, char[]> userPasswordMap = new HashMap<>();

                String username = configuration.getString(ConfigurationKey.SERVER_AUTHENTICATION_BASIC_USERNAME).trim();
                String password = configuration.getString(ConfigurationKey.SERVER_AUTHENTICATION_BASIC_PASSWORD).trim();

                IdentityManager identityManager = new SimpleIdentityManager(username, password);
                httpHandler = addSecurity(httpHandler, identityManager);
            }

            undertowBuilder.setHandler(httpHandler);

            Integer workerThreads = configuration.getInteger(ConfigurationKey.SERVER_WORKER_THREADS);
            if (workerThreads != null) {
                undertowBuilder.setWorkerThreads(workerThreads);
            }

            Integer ioThreads = configuration.getInteger(ConfigurationKey.SERVER_IO_THREADS);
            if (ioThreads != null) {
                undertowBuilder.setIoThreads(ioThreads);
            }

            this.undertow = undertowBuilder.build();

            LOGGER.info("staring web server...");

            this.undertow.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (undertow != null) {
                    try {
                        undertow.stop();
                    } catch (Exception e) {
                        // DO NOTHING
                    }
                }
            }));

            LOGGER.info("running...");
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("exiting");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("exiting");
            System.exit(1);
        }
    }

    /**
     * Method to validate configuration
     *
     * @param configuration
     */
    private void validate(Configuration configuration) {
        validate(
                ConfigurationKey.SERVER_PORT,
                configuration.getString(ConfigurationKey.SERVER_PORT));

        Integer serverPort = configuration.getInteger(ConfigurationKey.SERVER_PORT);

        validate(ConfigurationKey.SERVER_PORT, serverPort, 1, 65535);

        validate(
                ConfigurationKey.PROMETHEUS_YAML,
                configuration.getString(ConfigurationKey.PROMETHEUS_YAML));

        validate(
                ConfigurationKey.SERVER_HOST,
                configuration.getString(ConfigurationKey.SERVER_HOST));

        // TODO validate IP or hostname format

        // Optional
        Integer workerThreads = configuration.getInteger(ConfigurationKey.SERVER_WORKER_THREADS);

        if (workerThreads != null) {
            validate(ConfigurationKey.SERVER_WORKER_THREADS, workerThreads, 1, Integer.MAX_VALUE);
        }

        // Optional
        Integer ioThreads = configuration.getInteger(ConfigurationKey.SERVER_IO_THREADS);

        if (ioThreads != null) {
            validate(ConfigurationKey.SERVER_IO_THREADS, ioThreads, 1, Integer.MAX_VALUE);
        }

        Boolean basicAuthenticationEnabled =
                configuration.getBoolean(ConfigurationKey.SERVER_AUTHENTICATION_BASIC_ENABLED, Boolean.FALSE);

        if (basicAuthenticationEnabled) {
            validate(
                    ConfigurationKey.SERVER_AUTHENTICATION_BASIC_USERNAME,
                    configuration.getString(ConfigurationKey.SERVER_AUTHENTICATION_BASIC_USERNAME));

            validate(
                    ConfigurationKey.SERVER_AUTHENTICATION_BASIC_PASSWORD,
                    configuration.getString(ConfigurationKey.SERVER_AUTHENTICATION_BASIC_PASSWORD));
        }

        // Optional
        Boolean sslEnabled = configuration.getBoolean(ConfigurationKey.SERVER_SSL_ENABLED);

        if (sslEnabled) {
            validate(
                    ConfigurationKey.SERVER_SSL_PROTOCOL,
                    configuration.getString(ConfigurationKey.SERVER_SSL_PROTOCOL, DEFAULT_SERVER_SSL_PROTOCOL));

            validate(
                    ConfigurationKey.SERVER_SSL_KEYSTORE,
                    configuration.getString(ConfigurationKey.SERVER_SSL_KEYSTORE));

            validate(
                    ConfigurationKey.SERVER_SSL_KEYSTORE_TYPE,
                    configuration.getString(ConfigurationKey.SERVER_SSL_KEYSTORE_TYPE, DEFAULT_SERVER_SSL_KEYSTORE_TYPE));

            validate(
                    ConfigurationKey.SERVER_SSL_KEYSTORE_PASSWORD,
                    configuration.getString(ConfigurationKey.SERVER_SSL_KEYSTORE_PASSWORD));

            validate(
                    ConfigurationKey.SERVER_SSL_CERTIFICATE_ALIAS,
                    configuration.getString(ConfigurationKey.SERVER_SSL_CERTIFICATE_ALIAS));
        }
    }

    /**
     * Method to validate a String configuration property
     *
     * @param name
     * @param value
     */
    private void validate(String name, String value) {
        if (value == null) {
            throw new IllegalArgumentException("property [" + name + "] is null");
        }

        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("property [" + name + "] is empty");
        }
    }

    /**
     * Method to validate an Integer configuration property
     *
     * @param name
     * @param value
     * @param min
     * @param max
     */
    private void validate(String name, Integer value, Integer min, Integer max) {
        if (value == null) {
            throw new IllegalArgumentException("property [" + name + "] is null");
        }

        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    "property [" + name + "] is out of range (" + min + " - " + max + ")");
        }
    }

    /**
     * Method to validate a File
     *
     * @param name
     * @param file
     */
    private void validate(String name, File file) {
        if (file == null) {
            throw new IllegalArgumentException("file [" + name + "] is null");
        }

        if (file == null) {
            throw new IllegalArgumentException("file [" + name + "] = [" + file.getAbsolutePath() + "] is null");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("file [" + name + "] = [" + file.getAbsolutePath() + "] doesn't exist");
        }

        if (!file.canRead()) {
            throw new IllegalArgumentException("file [" + name + "] = [" + file.getAbsolutePath() + "] isn't readable");
        }
    }

    private SSLContext createSSLContext(Configuration configuration) throws Exception {
        File keyStoreFile = new File(
                configuration.getString(ConfigurationKey.SERVER_SSL_KEYSTORE)).getAbsoluteFile();

        validate(ConfigurationKey.SERVER_SSL_KEYSTORE, keyStoreFile);

        String keyStorePassword = configuration.getString(ConfigurationKey.SERVER_SSL_KEYSTORE_PASSWORD);
        validate(ConfigurationKey.SERVER_SSL_KEYSTORE_PASSWORD, keyStorePassword);

        String keyStoreType = configuration.getString(ConfigurationKey.SERVER_SSL_KEYSTORE_TYPE, DEFAULT_SERVER_SSL_KEYSTORE_TYPE);
        validate(ConfigurationKey.SERVER_SSL_KEYSTORE_TYPE, keyStoreType);

        String sslProtocol = configuration.getString(ConfigurationKey.SERVER_SSL_PROTOCOL);
        validate(ConfigurationKey.SERVER_SSL_PROTOCOL, sslProtocol);

        String certificateAlias = configuration.getString(ConfigurationKey.SERVER_SSL_CERTIFICATE_ALIAS);
        validate(ConfigurationKey.SERVER_SSL_CERTIFICATE_ALIAS, certificateAlias);

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
            throw new IllegalArgumentException("keystore [" + ConfigurationKey.SERVER_SSL_KEYSTORE + "] = [" + keyStoreFile.getAbsolutePath() + "] doesn't contain certificate");
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
     * @param toWrap
     * @param identityManager
     * @return
     */
    private static HttpHandler addSecurity(final HttpHandler toWrap, final IdentityManager identityManager) {
        HttpHandler handler = toWrap;
        handler = new AuthenticationCallHandler(handler);
        handler = new AuthenticationConstraintHandler(handler);
        List<AuthenticationMechanism> mechanisms = Collections.<AuthenticationMechanism>singletonList(new BasicAuthenticationMechanism("/"));
        handler = new AuthenticationMechanismsHandler(handler, mechanisms);
        handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, handler);
        return handler;
    }

    /**
     * Class to dispatch requests to handlers
     */
    private class DispatchingHttpHandler implements HttpHandler {

        private HttpHandler healthyHttpHandler = new HealthyHttpHandler();
        private HttpHandler faviconHttpHandler = new FaviconHttpHandler();
        private HttpHandler defaultHttpHandler = new DefaultHttpHandler();

        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            // Add headers to prevent caching
            httpServerExchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "no-cache");
            httpServerExchange.getResponseHeaders().put(Headers.EXPIRES, "0");
            httpServerExchange.getResponseHeaders().put(Headers.PRAGMA, "no-cache");

            // Dispatch the httpServerExchange
            String requestPath = httpServerExchange.getRequestPath();
            if (requestPath.startsWith("/-/healthy")) {
                healthyHttpHandler.handleRequest(httpServerExchange);
            } else if (requestPath.equals("/favicon.ico")) {
                faviconHttpHandler.handleRequest(httpServerExchange);
            } else {
                defaultHttpHandler.handleRequest(httpServerExchange);
            }
        }
    }

    /**
     * Class to handle a "/-/healthy" request
     */
    private class HealthyHttpHandler implements HttpHandler {

        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            httpServerExchange.getResponseSender().send("Exporter is healthy.");
        }
    }

    /**
     * Class to handle a "/favicon.ico" request
     */
    private class FaviconHttpHandler implements HttpHandler {

        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            httpServerExchange.setStatusCode(404);
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            httpServerExchange.getResponseSender().send("404 NOT FOUND");
        }
    }

    /**
     * Class to handle all other requests not handled
     */
    private class DefaultHttpHandler implements HttpHandler {

        @Override
        public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
            HeaderValues headerValues = httpServerExchange.getRequestHeaders().get(Headers.ACCEPT);

            String acceptHeader = null;
            if (headerValues != null) {
                acceptHeader = headerValues.get(0);
            }

            String contentType = TextFormat.chooseContentType(acceptHeader);
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);

            StringWriter stringWriter = new StringWriter(1024);
            TextFormat.writeFormat(contentType, stringWriter, CollectorRegistry.defaultRegistry.metricFamilySamples());
            httpServerExchange.getResponseSender().send(stringWriter.toString());
        }
    }
}
