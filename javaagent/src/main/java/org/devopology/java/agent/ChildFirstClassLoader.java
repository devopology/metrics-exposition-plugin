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

package org.devopology.java.agent;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to implement a child first ClassLoader for isolation
 */
public class ChildFirstClassLoader extends URLClassLoader {

    private final ClassLoader sysClzLoader;

    /**
     * Constructor
     *
     * @param urls
     * @param parent
     */
    public ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        sysClzLoader = getSystemClassLoader();
    }

    /**
     * Method to load a Class
     *
     * @param name
     *         The <a href="#binary-name">binary name</a> of the class
     *
     * @param resolve
     *         If {@code true} then resolve the class
     *
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // has the class loaded already?
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            try {
                if (sysClzLoader != null) {
                    loadedClass = sysClzLoader.loadClass(name);
                }
            } catch (ClassNotFoundException ex) {
                // class not found in system class loader... silently skipping
            }

            try {
                // find the class from given jar urls as in first constructor parameter.
                if (loadedClass == null) {
                    loadedClass = findClass(name);
                }
            } catch (ClassNotFoundException e) {
                // class is not found in the given urls.
                // Let's try it in parent classloader.
                // If class is still not found, then this method will throw class not found ex.
                loadedClass = super.loadClass(name, resolve);
            }
        }

        if (resolve) {      // marked to resolve
            resolveClass(loadedClass);
        }
        return loadedClass;
    }

    /**
     * Method to get resources
     *
     * @param name
     *         The resource name
     *
     * @return
     * @throws IOException
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> allRes = new LinkedList<>();

        // load resources from sys class loader
        Enumeration<URL> sysResources = sysClzLoader.getResources(name);
        if (sysResources != null) {
            while (sysResources.hasMoreElements()) {
                allRes.add(sysResources.nextElement());
            }
        }

        // load resource from this classloader
        Enumeration<URL> thisRes = findResources(name);
        if (thisRes != null) {
            while (thisRes.hasMoreElements()) {
                allRes.add(thisRes.nextElement());
            }
        }

        // then try finding resources from parent classloaders
        Enumeration<URL> parentRes = super.findResources(name);
        if (parentRes != null) {
            while (parentRes.hasMoreElements()) {
                allRes.add(parentRes.nextElement());
            }
        }

        return new Enumeration<URL>() {
            Iterator<URL> it = allRes.iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public URL nextElement() {
                return it.next();
            }
        };
    }

    /**
     * Method to get a URL to a resource
     *
     * @param name
     *         The resource name
     *
     * @return
     */
    @Override
    public URL getResource(String name) {
        URL res = null;
        if (sysClzLoader != null) {
            res = sysClzLoader.getResource(name);
        }
        if (res == null) {
            res = findResource(name);
        }
        if (res == null) {
            res = super.getResource(name);
        }
        return res;
    }
}