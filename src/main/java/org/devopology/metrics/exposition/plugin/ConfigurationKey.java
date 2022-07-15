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

public class ConfigurationKey {

    public static final String SERVER_HOST = "server.host";
    public static final String SERVER_PORT = "server.port";
    public static final String SERVER_WORKER_THREADS = "server.worker.threads";
    public static final String SERVER_IO_THREADS = "server.io.threads";
    public static final String SERVER_AUTHENTICATION_BASIC_ENABLED = "server.authentication.basic.enabled";
    public static final String SERVER_AUTHENTICATION_BASIC_USERNAME = "server.authentication.basic.username";
    public static final String SERVER_AUTHENTICATION_BASIC_PASSWORD = "server.authentication.basic.password";
    public static final String SERVER_SSL_ENABLED = "server.ssl.enabled";
    public static final String SERVER_SSL_PROTOCOL = "server.ssl.protocol";
    public static final String SERVER_SSL_KEYSTORE = "server.ssl.keystore";
    public static final String SERVER_SSL_KEYSTORE_TYPE = "server.ssl.keystore.type";
    public static final String SERVER_SSL_KEYSTORE_PASSWORD = "server.ssl.keystore.password";
    public static final String SERVER_SSL_CERTIFICATE_ALIAS = "server.ssl.certificate.alias";
    public static final String PROMETHEUS_YAML = "prometheus.yaml";
    public static final String DEFAULT_EXPORTERS_ENABLED = "default.exporters.enabled";
}
