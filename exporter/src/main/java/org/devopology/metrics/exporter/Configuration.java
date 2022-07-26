package org.devopology.metrics.exporter;

import org.devopology.common.converter.Converter;
import org.devopology.common.converter.ConverterException;
import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;
import org.devopology.common.yamlpath.YamlPath;
import org.devopology.common.yamlpath.PathNotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static final int OBJECT_NOT_NULL = 0;
    private static final int OBJECT_NULL_AND_REQUIRED = 1;
    private static final int OBJECT_NULL_AND_NOT_REQUIRED = 2;

    private YamlPath yamlPath;

    public Configuration() {
        // DO NOTHING
    }

    public void load(Reader reader) throws IOException {
        this.yamlPath = YamlPath.parse(reader);
    }

    public Boolean getBoolean(String path) throws ConfigurationException {
        return getBoolean(path, true);
    }

    public Boolean getBoolean(String path, boolean isRequired) throws ConfigurationException {
        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return Converter.BOOLEAN.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format("path [%2] value is required", path));
                }
                case OBJECT_NULL_AND_NOT_REQUIRED: {
                    return Boolean.FALSE;
                }
            }
        } catch (PathNotFoundException e) {
            if (isRequired) {
                throw new ConfigurationException(e.getMessage());
            }
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }

        return Boolean.FALSE;
    }

    public Integer getInteger(String path) throws ConfigurationException {
        return getInteger(path, true);
    }

    public Integer getInteger(String path, boolean isRequired) throws ConfigurationException {
        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return Converter.INTEGER.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format("path [%2] value is required", path));
                }
                case OBJECT_NULL_AND_NOT_REQUIRED: {
                    return null;
                }
            }
        } catch (PathNotFoundException e) {
            if (isRequired) {
                throw new ConfigurationException(e.getMessage());
            }
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }

        return null;
    }

    public Long getLong(String path) throws ConfigurationException {
        return getLong(path, true);
    }

    public Long getLong(String path, boolean isRequired) throws ConfigurationException {
        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return Converter.LONG.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format("path [%2] value is required", path));
                }
                case OBJECT_NULL_AND_NOT_REQUIRED: {
                    return null;
                }
            }
        } catch (PathNotFoundException e) {
            if (isRequired) {
                throw new ConfigurationException(e.getMessage());
            }
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }

        return null;
    }

    public String getString(String path) throws ConfigurationException {
        return getString(path, true);
    }

    public String getString(String path, boolean isRequired) throws ConfigurationException {
        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return Converter.STRING.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format("path [%2] value is required", path));
                }
                case OBJECT_NULL_AND_NOT_REQUIRED: {
                    return null;
                }
            }
        } catch (PathNotFoundException e) {
            if (isRequired) {
                throw new ConfigurationException(e.getMessage());
            }
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }

        return null;
    }

    public String getHostOrIPAddress(String path) throws ConfigurationException {
        return getHostOrIPAddress(path, true);
    }

    public String getHostOrIPAddress(String path, boolean isRequired) throws ConfigurationException {
        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return Converter.STRING.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format("path [%2] value is required", path));
                }
                case OBJECT_NULL_AND_NOT_REQUIRED: {
                    return null;
                }
            }
        } catch (PathNotFoundException e) {
            if (isRequired) {
                throw new ConfigurationException(e.getMessage());
            }
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }

        return null;
    }

    public File getReadableFile(String path) throws ConfigurationException {
        return getReadableFile(path, true);
    }

    public File getReadableFile(String path, boolean isRequired) throws ConfigurationException {
        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return Converter.READABLE_FILE.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format("path [%2] value is required", path));
                }
                case OBJECT_NULL_AND_NOT_REQUIRED: {
                    return null;
                }
            }
        } catch (PathNotFoundException e) {
            if (isRequired) {
                throw new ConfigurationException(e.getMessage());
            }
        } catch (ConverterException e) {
            throw new ConfigurationException(e.getMessage());
        }

        return null;
    }

    private int state(Object object, boolean isRequired) {
        if (object != null) {
            return OBJECT_NOT_NULL;
        }

        if (isRequired) {
            return OBJECT_NULL_AND_REQUIRED;
        } else {
            return OBJECT_NULL_AND_NOT_REQUIRED;
        }
    }
}
