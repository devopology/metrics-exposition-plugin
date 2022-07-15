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

package org.devopology.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class to implement configuration
 */
public class Configuration {

    private Map<String, String> map;

    /**
     * Constructor
     */
    public Configuration() {
        this.map = new TreeMap();
    }

    /**
     * Method to load a Map
     *
     * @param map
     * @return
     */
    public Configuration load(Map<String, String> map) {
        checkNull(map, "map is null");
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            this.map.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Method to a load a Properties file
     *
     * @param file
     * @return Configuration
     * @throws IOException
     */
    public Configuration load(File file) throws IOException {
        checkNull(file, "file is null");
        Properties tempProperties = new Properties();
        tempProperties.load(new FileReader(file));
        Set<Map.Entry<Object, Object>> entrySet = tempProperties.entrySet();
        for (Map.Entry<Object, Object> entry : entrySet) {
            set((String) entry.getKey(), (String) entry.getValue());
        }
        return this;
    }

    /**
     * Method to merge configuration objects
     *
     * @param configuration
     * @return Configuration
     */
    public Configuration merge(Configuration configuration) {
        checkNull(configuration, "configuration is null");
        load(configuration.map);
        return this;
    }

    /**
     * Method to return whether configuration contains a key
     *
     * @param key
     * @return true if the key exists, false if the key doesn't exist
     */
    public boolean containsKey(String key) {
        checkNull(key, "key is null");
        return this.map.containsKey(key);
    }

    /**
     * Method to remove a configuration key
     *
     * @param key
     * @return value if the key exists
     */
    public String removeKey(String key) {
        checkNull(key, "key is null");
        return this.map.remove(key);
    }

    /**
     * Method to set a configuration key / value
     *
     * @param key
     * @param value
     * @return Configuration
     */
    public Configuration set(String key, String value) {
        checkNull(key, "key is null");
        checkNull(value, "value is null");
        this.map.put(key, value);
        return this;
    }

    /**
     * Method to set a configuration key / value
     *
     * @param key
     * @param value
     * @return Configuration
     */
    public Configuration set(String key, Integer value) {
        checkNull(key, "key is null");
        checkNull(value, "value is null");
        return set(key, value.toString());
    }

    /**
     * Method to set a configuration key / value if the key is absent
     *
     * @param key
     * @param value
     * @return Configuration
     */
    public Configuration setIfAbsent(String key, String value) {
        checkNull(key, "key is null");
        checkNull(value, "value is null");
        if (!this.map.containsKey(key)) {
            this.map.put(key, value);
        }
        return this;
    }

    /**
     * Method to set a configuration key / value if the key is absent
     *
     * @param key
     * @param value
     * @return Configuration
     */
    public Configuration setIfAbsent(String key, Integer value) {
        checkNull(key, "key is null");
        checkNull(value, "value is null");
        if (!this.map.containsKey(key)) {
            this.map.put(key, value.toString());
        }
        return this;
    }

    /**
     * Method to get a configuration value as a String
     *
     * @param key
     * @return String
     */
    public String getString(String key) {
        checkNull(key, "key is null");
        return getString(key, null);
    }

    /**
     * Method to get a configuration value as a String
     *
     * @param key
     * @param defaultValue
     * @return String
     */
    public String getString(String key, String defaultValue) {
        checkNull(key, "key is null");
        String result = this.map.get(key);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Method to get a configuration value as an Integer
     * <p>
     * Returns null if the configuration value can't be converted to an Integer
     *
     * @param key
     * @return Integer
     */
    public Integer getInteger(String key) {
        checkNull(key, "key is null");
        return getInteger(key, null);
    }

    /**
     * Method to get a configuration value as an Integer
     * <p>
     * Returns the defaultValue if the configuration value can't be converted to an Integer
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public Integer getInteger(String key, Integer defaultValue) {
        checkNull(key, "key is null");
        Integer result = defaultValue;
        if (this.map.containsKey(key)) {
            try {
                result = Integer.parseInt(this.map.get(key));
            } catch (NumberFormatException e) {
                // DO NOTHING
            }
        }
        return result;
    }

    /**
     * Method to get a configuration value as a Boolean
     * <p>
     * Returns false if the key doesn't exist or doesn't equal "true"
     *
     * @param key
     * @return
     */
    public Boolean getBoolean(String key) {
        checkNull(key, "key is null");
        return getBoolean(key, false);
    }

    /**
     * Method to get a configuration value as a Boolean
     * <p>
     * Returns the defaultValue if the key doesn't exist
     * @param key
     * @param defaultValue
     * @return
     */
    public Boolean getBoolean(String key, boolean defaultValue) {
        checkNull(key, "key is null");
        boolean result = defaultValue;
        if (this.map.containsKey(key)) {
            String value = getString(key, "false");
            if (value.equalsIgnoreCase("true")) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Method to get a deep copy as a Map
     * @return
     */
    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<String, String>(this.map.size());
        for (Map.Entry<String, String> entry : this.map.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private void checkNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}
