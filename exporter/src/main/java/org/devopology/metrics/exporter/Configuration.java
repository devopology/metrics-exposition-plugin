package org.devopology.metrics.exporter;

import org.devopology.common.converter.Converter;
import org.devopology.common.converter.ConverterException;
import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;
import org.devopology.common.resolver.Resolver;
import org.devopology.common.resolver.ResolverException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class Configuration {

    private final static Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private Map<String, Object> yamlMap;

    public Configuration() {
        // DO NOTHING
    }

    public void load(Reader reader) throws IOException {
        // Load the YAML configuration using a custom Resolver to return String values
        Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new LoaderOptions(), new org.yaml.snakeyaml.resolver.Resolver() {
            protected void addImplicitResolvers() {
                this.addImplicitResolver(Tag.MERGE, MERGE, "<");
                this.addImplicitResolver(Tag.YAML, YAML, "!&*");
            }
        });

        this.yamlMap = yaml.load(reader);
    }

    /*
    public void validate() throws ConfigurationException {
        try {
            Object object;
            String path = null;

            path = Constant.EXPORTER_SERVER_HOST_PATH;
            object = Resolver.OBJECT.resolve(path, yamlMap);
            String serverHost = Converter.HOST_OR_IP_ADDRESS.convert(object, Converter.Required.TRUE, path);
            LOGGER.info(String.format("host = [%s]", serverHost));

            path = Constant.EXPORTER_SERVER_PORT_PATH;
            object = Resolver.OBJECT.resolve(path, yamlMap);
            Integer serverPort = Converter.INTEGER.convert(object, Converter.Required.TRUE, path);
            LOGGER.info(String.format("port = [%d]", serverPort));

            path = Constant.EXPORTER_SERVER_THREADS_IO_PATH;
            object = Resolver.OBJECT.resolve(path, yamlMap);
            Integer ioThreads = Converter.INTEGER.convert(object, Converter.Required.FALSE, path);
            if (ioThreads != null) {
                if (ioThreads < 1) {
                    throw new ConverterException(String.format("%s = [%d] must be greater than 0", path, ioThreads));
                }
                LOGGER.info(String.format("IO threads = [%d]", ioThreads));
            }

            path = Constant.EXPORTER_SERVER_THREADS_WORKER_PATH;
            object = Resolver.OBJECT.resolve(path, yamlMap);
            Integer workerThreads = Converter.INTEGER.convert(object, Converter.Required.FALSE, path);
            if (workerThreads != null) {
                if (workerThreads < 1) {
                    throw new ConverterException(String.format("%s = [%d] must be greater than 0", path, workerThreads));
                }
                LOGGER.info(String.format("worker threads = [%d]", workerThreads));
            }

            Long cacheMilliseconds = null;

            path = Constant.EXPORTER_SERVER_CACHE_ENABLED_PATH;
            object = Resolver.OBJECT.resolve(path, yamlMap);
            Boolean isCachingEnabled = Converter.BOOLEAN.convert(object, Converter.Required.FALSE, path);
            LOGGER.info(String.format("caching enabled = [%b]", isCachingEnabled));

            if (isCachingEnabled) {
                path = Constant.EXPORTER_SERVER_CACHE_MILLISECONDS_PATH;
                object = Resolver.OBJECT.resolve(path, yamlMap);
                cacheMilliseconds = Converter.LONG.convert(object, Converter.Required.TRUE, path);

                LOGGER.info(String.format("cache milliseconds = [%d]", cacheMilliseconds));
            }

            path = Constant.EXPORTER_SERVER_AUTHENTICATION_BASIC_ENABLED_PATH;
            object = Resolver.OBJECT.resolve(path, yamlMap);
            Boolean isBasicAuthenticationEnabled = Converter.BOOLEAN.convert(object, Converter.Required.FALSE, path);
            LOGGER.info(String.format("BASIC authentication enabled = [%b]", isBasicAuthenticationEnabled));

            String basicAuthenticationUsername = null;
            String basicAuthenticationPassword = null;

            if (isBasicAuthenticationEnabled) {
                path = Constant.EXPORTER_SERVER_AUTHENTICATION_BASIC_USERNAME_PATH;
                object = Resolver.OBJECT.resolve(path, yamlMap);
                basicAuthenticationUsername = Converter.STRING.convert(object, Converter.Required.TRUE, path);
                LOGGER.info(String.format("BASIC authentication username = [%s]", basicAuthenticationUsername));

                path = Constant.EXPORTER_SERVER_AUTHENTICATION_BASIC_PASSWORD_PATH;
                object = Resolver.OBJECT.resolve(path, yamlMap);
                basicAuthenticationPassword = Converter.STRING.convert(object, Converter.Required.TRUE, path);
                LOGGER.info(String.format("BASIC authentication password = [%s] (SHA1 hash)", SHA1.hash(basicAuthenticationPassword)));
            }

            File keyStore = null;
            String keyStoreType = null;
            String keyStorePassword = null;
            String certificateAlias = null;
            String sslProtocol = null;

            path = Constant.EXPORTER_SERVER_SSL_ENABLED_PATH;
            object = Resolver.OBJECT.resolve(path, yamlMap);
            Boolean isSSLEnabled = Converter.BOOLEAN.convert(object, Converter.Required.FALSE, path);
            LOGGER.info(String.format("SSL enabled = [%b]", isSSLEnabled));

            if (isSSLEnabled) {
                path = Constant.EXPORTER_SERVER_SSL_KEYSTORE_FILENAME_PATH;
                object = Resolver.OBJECT.resolve(path, yamlMap);
                keyStore = Converter.READABLE_FILE.convert(object, Converter.Required.TRUE, path);
                LOGGER.info(String.format("keystore filename = [%s]", keyStore));

                path = Constant.EXPORTER_SERVER_SSL_KEYSTORE_TYPE_PATH;
                object = Resolver.OBJECT.resolve(path, yamlMap);
                keyStoreType = Converter.STRING.convert(object, Converter.Required.TRUE, path);
                LOGGER.info(String.format("keystore type = [%s]", keyStoreType));

                path = Constant.EXPORTER_SERVER_SSL_KEYSTORE_PASSWORD_PATH;
                object = Resolver.OBJECT.resolve(path, yamlMap);
                keyStorePassword = Converter.STRING.convert(object, Converter.Required.TRUE, path);
                LOGGER.info(String.format("keystore password = [%s] (SHA1 hash)", SHA1.hash(keyStorePassword)));

                path = Constant.EXPORTER_SERVER_SSL_CERTIFICATE_ALIAS_PATH;
                object = Resolver.OBJECT.resolve(path, yamlMap);
                certificateAlias = Converter.STRING.convert(object, Converter.Required.TRUE, path);
                LOGGER.info(String.format("certificate alias = [%s]", certificateAlias));

                path = Constant.EXPORTER_SERVER_SSL_PROTOCOL_PATH;
                object = Resolver.OBJECT.resolve(path, yamlMap);
                sslProtocol = Converter.STRING.convert(object, Converter.Required.TRUE, path);
                LOGGER.info(String.format("SSL protocol = [%s]", sslProtocol));
            }

            path = Constant.EXPORTER_SERVER_HOTSPOT_EXPORTS_ENABLED_PATH;
            object = Resolver.OBJECT.resolve(path, yamlMap);
            Boolean isHotSpotExporterEnabled = Converter.BOOLEAN.convert(object, Converter.Required.FALSE, path);
            LOGGER.info(String.format("HotSpot exports enabled = [%b]", isHotSpotExporterEnabled));

            path = Constant.EXPORTER_SERVER_JMX_EXPORTER_ENABLED_PATH;
            object = Resolver.OBJECT.resolve(path, yamlMap);
            Boolean isJMXExporterEnabled = Converter.BOOLEAN.convert(object, Converter.Required.FALSE, path);
            LOGGER.info(String.format("JMX exports enabled = [%b]", isJMXExporterEnabled));

            File jmxExporterYamlFile = null;

            if (isJMXExporterEnabled) {
                path = Constant.EXPORTER_SERVER_JMX_EXPORTER_FILENAME_PATH;
                object = Resolver.OBJECT.resolve(path, yamlMap);
                jmxExporterYamlFile = Converter.READABLE_FILE.convert(object, Converter.Required.TRUE, path);
                LOGGER.info(String.format("JMX exporter filename = [%s]", jmxExporterYamlFile));
            }
        } catch (ResolverException e) {
            throw new ConfigurationException(e.getMessage());
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }
    */

    public Boolean getBoolean(String path) throws ConfigurationException {
        return getBoolean(path, true);
    }

    public Boolean getBoolean(String path, boolean required) throws ConfigurationException {
        Converter.Required converterRequired = Converter.Required.FALSE;

        if (required) {
            converterRequired = Converter.Required.TRUE;
        }

        try {
            Object object = Resolver.OBJECT.resolve(path, yamlMap);
            return Converter.BOOLEAN.convert(object, converterRequired, path);
        } catch (ResolverException e) {
            throw new ConfigurationException(e.getMessage());
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    public Integer getInteger(String path) throws ConfigurationException {
        return getInteger(path, true);
    }

    public Integer getInteger(String path, boolean required) throws ConfigurationException {
        Converter.Required converterRequired = Converter.Required.FALSE;

        if (required) {
            converterRequired = Converter.Required.TRUE;
        }

        try {
            Object object = Resolver.OBJECT.resolve(path, yamlMap);
            return Converter.INTEGER.convert(object, converterRequired, path);
        } catch (ResolverException e) {
            throw new ConfigurationException(e.getMessage());
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    public Long getLong(String path) throws ConfigurationException {
        return getLong(path, true);
    }

    public Long getLong(String path, boolean required) throws ConfigurationException {
        Converter.Required converterRequired = Converter.Required.FALSE;

        if (required) {
            converterRequired = Converter.Required.TRUE;
        }

        try {
            Object object = Resolver.OBJECT.resolve(path, yamlMap);
            return Converter.LONG.convert(object, converterRequired, path);
        } catch (ResolverException e) {
            throw new ConfigurationException(e.getMessage());
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    public String getString(String path) throws ConfigurationException {
        return getString(path, true);
    }

    public String getString(String path, boolean required) throws ConfigurationException {
        Converter.Required converterRequired = Converter.Required.FALSE;

        if (required) {
            converterRequired = Converter.Required.TRUE;
        }

        try {
            Object object = Resolver.OBJECT.resolve(path, yamlMap);
            return Converter.STRING.convert(object, converterRequired, path);
        } catch (ResolverException e) {
            throw new ConfigurationException(e.getMessage());
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    public String getHostOrIPAddress(String path) throws ConfigurationException {
        return getHostOrIPAddress(path, true);
    }

    public String getHostOrIPAddress(String path, boolean required) throws ConfigurationException {
        Converter.Required converterRequired = Converter.Required.FALSE;

        if (required) {
            converterRequired = Converter.Required.TRUE;
        }

        try {
            Object object = Resolver.OBJECT.resolve(path, yamlMap);
            return Converter.HOST_OR_IP_ADDRESS.convert(object, converterRequired, path);
        } catch (ResolverException e) {
            throw new ConfigurationException(e.getMessage());
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    public File getReadableFile(String path) throws ConfigurationException {
        return getReadableFile(path, true);
    }

    public File getReadableFile(String path, boolean required) throws ConfigurationException {
        Converter.Required converterRequired = Converter.Required.FALSE;

        if (required) {
            converterRequired = Converter.Required.TRUE;
        }

        try {
            Object object = Resolver.OBJECT.resolve(path, yamlMap);
            return Converter.READABLE_FILE.convert(object, converterRequired, path);
        } catch (ResolverException e) {
            throw new ConfigurationException(e.getMessage());
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }
}
