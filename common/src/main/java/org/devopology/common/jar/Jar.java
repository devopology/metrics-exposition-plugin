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

package org.devopology.common.jar;

import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;
import org.devopology.common.precondition.Precondition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarInputStream;

/**
 * Class to load a jar and cache entries in memory. Only files in the jar are loaded.
 * <p>
 * NOTE: clear() should be called to conserve memory after
 * retrieving the entries you require and is no longer used
 */
public class Jar {

    private static final Logger LOGGER = LoggerFactory.getLogger(Jar.class);

    private Map<String, JarEntry> jarEntryMap;

    /**
     * Constructor
     */
    public Jar() {
        jarEntryMap = new TreeMap<>();
    }

    /**
     * Load a Jar file, clearing any previously loaded entries
     *
     * @param file
     * @throws IOException
     */
    public void load(File file) throws IOException {
        Precondition.notNull(file, "file is null");

        clear();

        try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(file))) {
            load(jarInputStream);
        }
    }

    /**
     * Load a JarInputStream, clearing any previously loaded entries
     *
     * @param jarInputStream
     * @throws IOException
     */
    public void load(JarInputStream jarInputStream) throws IOException {
        Precondition.notNull(jarInputStream, "jarInputStream is null");

        clear();

        while (true) {
            java.util.jar.JarEntry jarEntry = jarInputStream.getNextJarEntry();
            if (jarEntry == null) {
                break;
            }

            // Only load jar entries that are files
            if (!jarEntry.isDirectory()) {
                byte[] bytes = new byte[1024];
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                while (true) {
                    int count = jarInputStream.read(bytes);
                    if (count == -1) {
                        break;
                    }

                    byteArrayOutputStream.write(bytes, 0, count);
                }

                byte[] jarEntryBytes = byteArrayOutputStream.toByteArray();
                jarEntryMap.put(jarEntry.getName(), new JarEntry(jarEntry, jarEntryBytes));
            }
        }
    }

    /**
     * Method to clear all previously loaded entries
     */
    public void clear() {
        jarEntryMap.clear();
    }

    /**
     * Method to return whether an entry exists
     *
     * @param key
     * @return
     */
    public boolean containsKey(String key) {
        return jarEntryMap.containsKey(key);
    }

    /**
     * Method to get a loaded entry
     *
     * @param key
     * @return
     */
    public JarEntry get(String key) {
        return jarEntryMap.get(key);
    }

    /**
     * Method to get a Set of entry keys
     *
     * @return
     */
    public Set<String> keySet() {
        return jarEntryMap.keySet();
    }

    /**
     * Method to get the Set of all entries
     * @return
     */
    public Set<Map.Entry<String, JarEntry>> entrySet() {
        return jarEntryMap.entrySet();
    }

    /**
     * Method to get the number of entries
     *
     * @return
     */
    public int size() {
        return jarEntryMap.size();
    }
}
