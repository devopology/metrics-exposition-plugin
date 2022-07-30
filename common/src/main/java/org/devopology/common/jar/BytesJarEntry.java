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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.jar.JarEntry;

/**
 * Class to implement a BytesJarEntry that contains the entries bytes
 */
public class BytesJarEntry extends JarEntry {

    private byte[] bytes;

    /**
     * Constructor
     *
     * @param jarEntry
     * @param bytes
     */
    BytesJarEntry(JarEntry jarEntry, byte[] bytes) {
        super(jarEntry.getName());
        this.bytes = bytes;
    }

    /**
     * Method to get an entries size in bytes
     *
     * @return
     */
    @Override
    public long getSize() {
        return this.bytes.length;
    }

    /**
     * Method to get an entries bytes
     *
     * @return
     */
    public byte[] getBytes() {
        return this.bytes;
    }

    /**
     * Method to get an InputStream to the entry's bytes
     *
     * @return
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.bytes);
    }

    public String toString() {
        return String.format("{ %s (%d) }", getName(), getSize());
    }
}
