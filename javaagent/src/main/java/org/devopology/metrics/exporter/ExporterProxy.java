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

package org.devopology.metrics.exporter;

import org.devopology.common.precondition.Precondition;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Class to implement a proxy for the Exporter
 */
@SuppressWarnings("unchecked")
public class ExporterProxy {

    private static final String ORG_DEVOPOLOGY_METRICS_EXPORTER__CLASSNAME = "org.devopology.metrics.exporter.Exporter";

    private Class clazz;
    private Object object;
    private Method start;
    private Method stop;

    /**
     * Constructor
     *
     * @param classLoader
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public ExporterProxy(ClassLoader classLoader) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Precondition.notNull(classLoader, "classLoader is null");

        clazz = classLoader.loadClass(ORG_DEVOPOLOGY_METRICS_EXPORTER__CLASSNAME);
        object = clazz.getDeclaredConstructor((Class[]) null).newInstance((Object[]) null);
        start = clazz.getDeclaredMethod("start", new Class[] { File.class });
        stop = clazz.getDeclaredMethod("stop", (Class[]) null);
    }

    /**
     * Method to start the Exporter
     *
     * @param file
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void start(File file) throws InvocationTargetException, IllegalAccessException {
        start.invoke(this.object, new Object[] { file });
    }

    /**
     * Method to stop the Exporter
     *
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void stop() throws InvocationTargetException, IllegalAccessException {
        stop.invoke(this.object, (Object[]) null);
    }
}
