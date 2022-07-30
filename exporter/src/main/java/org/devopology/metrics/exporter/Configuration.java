package org.devopology.metrics.exporter;

import com.google.common.io.CharStreams;
import org.devopology.common.converter.Converter;
import org.devopology.common.converter.ConverterException;
import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;
import org.devopology.common.precondition.Precondition;
import org.devopology.exporter.common.converter.HostOrIPAddressConverter;
import org.devopology.exporter.common.yamlpath.YamlPath;
import org.devopology.exporter.common.yamlpath.PathNotFoundException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static final HostOrIPAddressConverter HOST_OR_IP_ADDRESS_CONVERTER = new HostOrIPAddressConverter();

    private static final int OBJECT_NOT_NULL = 0;
    private static final int OBJECT_NULL_AND_REQUIRED = 1;
    private static final int OBJECT_NULL_AND_NOT_REQUIRED = 2;

    private static final String PATH_IS_NULL = "path is null";
    private static final String PATH_IS_EMPTY = "path is empty";
    private static final String PATH_VALUE_IS_REQUIRED = "path [%s] value is required";
    private static final String READER_IS_NULL = "reader is null";
    private static final String NO_DATA_LOADED = "no data";

    private YamlPath yamlPath;

    public Configuration() {
        // DO NOTHING
    }

    public void load(Reader reader) throws ConfigurationException, IOException {
        Precondition.notNull(reader, READER_IS_NULL);

        String contents = CharStreams.toString(reader);

        // Code to scan the YAML file verifying that a document separator doesn't exist on any line number > 1
        int lineIndex = 1;
        BufferedReader bufferedReader = new BufferedReader(new StringReader(contents));
        while (true) {
            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            }

            if ((line.equals("---") || line.equals("...")) && (lineIndex > 1)) {
                throw new ConfigurationException(
                        String.format(
                                "configuration contains a document separator [%s] on line %d",
                                line,
                                lineIndex));
            }

            lineIndex++;
        }

        this.yamlPath = YamlPath.parse(new StringReader(contents));
    }

    public Boolean getBoolean(String path) throws ConfigurationException {
        return getBoolean(path, true);
    }

    public Boolean getBoolean(String path, boolean isRequired) throws ConfigurationException {
        Precondition.notNull(path, PATH_IS_NULL);
        Precondition.notEmpty(path, PATH_IS_EMPTY);
        Precondition.checkState(this.yamlPath != null, NO_DATA_LOADED);

        path = path.trim();

        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return Converter.BOOLEAN.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format(PATH_VALUE_IS_REQUIRED, path));
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
        Precondition.notNull(path, PATH_IS_NULL);
        Precondition.notEmpty(path, PATH_IS_EMPTY);
        Precondition.checkState(this.yamlPath != null, NO_DATA_LOADED);

        path = path.trim();

        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return Converter.INTEGER.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format(PATH_VALUE_IS_REQUIRED, path));
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
        Precondition.notNull(path, PATH_IS_NULL);
        Precondition.notEmpty(path, PATH_IS_EMPTY);
        Precondition.checkState(this.yamlPath != null, NO_DATA_LOADED);

        path = path.trim();

        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return Converter.LONG.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format(PATH_VALUE_IS_REQUIRED, path));
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
        Precondition.notNull(path, PATH_IS_NULL);
        Precondition.notEmpty(path, PATH_IS_EMPTY);
        Precondition.checkState(this.yamlPath != null, NO_DATA_LOADED);

        path = path.trim();

        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return Converter.STRING.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format(PATH_VALUE_IS_REQUIRED, path));
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
        Precondition.notNull(path, PATH_IS_NULL);
        Precondition.notEmpty(path, PATH_IS_EMPTY);
        Precondition.checkState(this.yamlPath != null, NO_DATA_LOADED);

        path = path.trim();

        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return HOST_OR_IP_ADDRESS_CONVERTER.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format(PATH_VALUE_IS_REQUIRED, path));
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
        Precondition.notNull(path, PATH_IS_NULL);
        Precondition.notEmpty(path, PATH_IS_EMPTY);
        Precondition.checkState(this.yamlPath != null, NO_DATA_LOADED);

        path = path.trim();

        try {
            Object object = yamlPath.read(path, isRequired);
            int state = state(object, isRequired);
            switch (state) {
                case OBJECT_NOT_NULL: {
                    return Converter.READABLE_FILE.convert(object);
                }
                case OBJECT_NULL_AND_REQUIRED: {
                    throw new ConfigurationException(String.format(PATH_VALUE_IS_REQUIRED, path));
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
