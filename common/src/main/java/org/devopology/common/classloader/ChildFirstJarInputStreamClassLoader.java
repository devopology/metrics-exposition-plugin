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

package org.devopology.common.classloader;

import org.devopology.common.jar.JarEntry;
import org.devopology.common.jar.Jar;
import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;
import org.devopology.common.precondition.Precondition;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarInputStream;

/**
 * Class to implement a child-first JarInputStream classloader
 */
public class ChildFirstJarInputStreamClassLoader extends ClassLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChildFirstJarInputStreamClassLoader.class);

    protected Jar jar;
    protected Map<String, Class> classMap;

    /**
     * Constructor
     *
     * @param jarInputStream
     * @param parent
     */
    public ChildFirstJarInputStreamClassLoader(JarInputStream jarInputStream, ClassLoader parent) throws IOException {
        this(parent);

        Precondition.notNull(jarInputStream, "jarInputStream is null");

        jar = new Jar();
        jar.load(jarInputStream);
    }

    /**
     * Constructor
     *
     * @param parent
     */
    protected ChildFirstJarInputStreamClassLoader(ClassLoader parent) {
        super(parent);

        classMap = Collections.synchronizedMap(new TreeMap<>());
    }

    /**
     * Method to get the names of loaded classes
     *
     * @return
     */
    public Set<String> getClassNames() {
        return classMap.keySet();
    }

    /**
     * Method to find a class
     *
     * @param classname
     *         The <a href="#binary-name">binary name</a> of the class
     *
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public Class findClass(String classname) throws ClassNotFoundException {
        LOGGER.trace(String.format("findClass() classname = [%s]", classname));

        Class clazz = classMap.get(classname);

        if (clazz == null) {
            byte[] bytes = null;

            try {
                bytes = loadClassBytes(classname);
            } catch (IOException e) {
                throw new ClassNotFoundException(classname, e);
            }

            if (bytes != null) {
                clazz = defineClass(classname, bytes, 0, bytes.length);
                classMap.put(classname, clazz);
            }
        }

        if (clazz == null) {
            throw new ClassNotFoundException(classname);
        }

        return clazz;
    }

    /**
     * Method to get a classes bytes from the Jar
     *
     * @param classname
     * @return
     */
    private byte[] loadClassBytes(String classname) throws IOException {
        String jarEntryName = classname.replaceAll("\\.", "/") + ".class";

        JarEntry jarEntry = jar.get(jarEntryName);
        if (jarEntry != null) {
            byte[] bytes = new byte[(int) jarEntry.getSize()];
            DataInputStream dataInputStream = new DataInputStream(jarEntry.getInputStream());
            dataInputStream.readFully(bytes);
            return bytes;
        }

        return null;
    }

    /**
     * Method to get a resource as an InputStream
     *
     * @param name
     *         The resource name
     *
     * @return
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        LOGGER.trace(String.format("getResourceAsStream() name = [%s]", name));

        JarEntry jarEntry = jar.get(name);
        if (jarEntry != null) {
            return jarEntry.getInputStream();
        }

        return null;
    }
}
