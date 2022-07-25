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

    private final ClassLoader systemClassLoader;

    /**
     * Constructor
     *
     * @param urls
     * @param parentClassLoader
     */
    public ChildFirstClassLoader(URL[] urls, ClassLoader parentClassLoader) {
        super(urls, parentClassLoader);
        systemClassLoader = getSystemClassLoader();
    }

    /**
     * Method to load a Class
     *
     * @param classname
     *         The <a href="#binary-name">binary name</a> of the class
     *
     * @param resolve
     *         If {@code true} then resolve the class
     *
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    protected Class<?> loadClass(String classname, boolean resolve) throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(classname);
        if (loadedClass == null) {
            try {
                if (systemClassLoader != null) {
                    loadedClass = systemClassLoader.loadClass(classname);
                }
            } catch (ClassNotFoundException ex) {
                // class not found in system class loader... silently skipping
            }

            try {
                // find the class from given jar urls as in first constructor parameter.
                if (loadedClass == null) {
                    loadedClass = findClass(classname);
                }
            } catch (ClassNotFoundException e) {
                // class is not found in the given urls.
                // Let's try it in parent classloader.
                // If class is still not found, then this method will throw class not found ex.
                loadedClass = super.loadClass(classname, resolve);
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
     * @param resourceName
     *         The resource name
     *
     * @return
     * @throws IOException
     */
    @Override
    public Enumeration<URL> getResources(String resourceName) throws IOException {
        List<URL> urlList = new LinkedList<>();

        // load resources from sys class loader
        Enumeration<URL> sysResources = systemClassLoader.getResources(resourceName);
        if (sysResources != null) {
            while (sysResources.hasMoreElements()) {
                urlList.add(sysResources.nextElement());
            }
        }

        // load resource from this classloader
        Enumeration<URL> thisRes = findResources(resourceName);
        if (thisRes != null) {
            while (thisRes.hasMoreElements()) {
                urlList.add(thisRes.nextElement());
            }
        }

        // then try finding resources from parent classloaders
        Enumeration<URL> parentRes = super.findResources(resourceName);
        if (parentRes != null) {
            while (parentRes.hasMoreElements()) {
                urlList.add(parentRes.nextElement());
            }
        }

        return new Enumeration<URL>() {
            Iterator<URL> url = urlList.iterator();

            @Override
            public boolean hasMoreElements() {
                return url.hasNext();
            }

            @Override
            public URL nextElement() {
                return url.next();
            }
        };
    }

    /**
     * Method to get a URL to a resource
     *
     * @param resourceName
     *         The resource name
     *
     * @return
     */
    @Override
    public URL getResource(String resourceName) {
        URL url = null;
        if (systemClassLoader != null) {
            url = systemClassLoader.getResource(resourceName);
        }
        if (url == null) {
            url = findResource(resourceName);
        }
        if (url == null) {
            url = super.getResource(resourceName);
        }
        return url;
    }
}