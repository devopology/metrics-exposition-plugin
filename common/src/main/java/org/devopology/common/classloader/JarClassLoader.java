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

import org.devopology.common.jar.BytesJarEntry;
import org.devopology.common.jar.Jar;
import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class to implement a custom ClassLoader using a Jar file
 */
public class JarClassLoader extends ClassLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JarClassLoader.class);

    private Jar jar;
    private Map<String, Class> classMap;

    /**
     * Constructor
     *
     * @param jar
     * @param parent
     */
    public JarClassLoader(Jar jar, ClassLoader parent) {
        super(parent);
        this.jar = jar;
        this.classMap = Collections.synchronizedMap(new TreeMap<>());
    }

    /**
     * Method to get the names of loaded classes
     *
     * @return
     */
    public Set<String> getClassNames() {
        return this.classMap.keySet();
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

        Class clazz = this.classMap.get(classname);

        if (clazz == null) {
            byte[] classBytes = loadClassBytes(classname);

            if (classBytes != null) {
                clazz = defineClass(classname, classBytes, 0, classBytes.length);
                this.classMap.put(classname, clazz);
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
    private byte[] loadClassBytes(String classname) {
        String jarEntryName = classname.replaceAll("\\.", "/") + ".class";

        BytesJarEntry bytesJarEntry = this.jar.get(jarEntryName);
        if (bytesJarEntry != null) {
            return bytesJarEntry.getBytes();
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

        BytesJarEntry bytesJarEntry = this.jar.get(name);
        if (bytesJarEntry != null) {
            return bytesJarEntry.getInputStream();
        }

        return null;
    }
}
