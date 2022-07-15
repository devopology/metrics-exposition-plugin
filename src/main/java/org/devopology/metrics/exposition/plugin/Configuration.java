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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

public class Configuration {
    
    private Map<String, String> map;

    public Configuration() {
        this.map = new TreeMap();
    }

    public Configuration load(Map<String, String> map) {
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            this.map.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public Configuration load(File file) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(file));
        Set<Map.Entry<Object, Object>> entrySet = properties.entrySet();
        for (Map.Entry<Object, Object> entry : entrySet) {
            put((String) entry.getKey(), (String) entry.getValue());
        }
        return this;
    }

    public Configuration remove(String key) {
        this.map.remove(key);
        return this;
    }

    public Configuration put(String key, String value) {
        this.map.put(key, value);
        return this;
    }

    public Configuration put(String key, Integer value) {
        if (value != null) {
            return put(key, value.toString());
        }
        return null;
    }

    public Configuration put(Configuration configuration) {
        this.map.putAll(configuration.asMap());
        return this;
    }

    public Configuration putIfAbsent(String key, String value) {
        if (value != null) {
            this.map.putIfAbsent(key, value);
        }
        return this;
    }

    public Configuration putIfAbsent(String key, Integer value) {
        if (value != null) {
            this.map.putIfAbsent(key, value.toString());
        }
        return this;
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        String result = this.map.get(key);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    public Integer getInteger(String key) {
        return getInteger(key, null);
    }

    public Integer getInteger(String key, Integer defaultValue) {
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

    public Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public Boolean getBoolean(String key, boolean defaultValue) {
        boolean result = defaultValue;
        if (this.map.containsKey(key)) {
            String value = getString(key, "false");
            if (value.equalsIgnoreCase("true")) {
                result = true;
            }
        }
        return result;
    }

    public Map<String, String> asMap() {
        return this.map;
    }
}
