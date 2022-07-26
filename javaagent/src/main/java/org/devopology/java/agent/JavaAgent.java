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

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * Class to implement a pluggable javaagent
 */
public class JavaAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaAgent.class);

    /**
     * Premain javaagent method
     *
     * @param arguments
     * @param instrumentation
     * @throws Exception
     */
    public static void premain(String arguments, Instrumentation instrumentation) throws Exception {
        new JavaAgent().execute(arguments);
    }

    /**
     * Method to execute
     *
     * @param argument
     */
    @SuppressWarnings("unchecked")
    public void execute(String argument) {
        try {
            File pwdFile = new File(".");
            String pwd = pwdFile.getAbsoluteFile().getParent() + "/";

            LOGGER.info("version " + Version.getVersion());
            LOGGER.info("starting");
            LOGGER.info("argument = [" + argument + "]");

            LOGGER.info("Java version " + System.getProperty("java.version"));

            if (argument == null) {
                throw new IllegalArgumentException("argument is null");
            }

            argument = argument.trim();

            if (argument.equals("\"\"")) {
                throw new IllegalArgumentException("argument is empty");
            }

            if (argument.startsWith("\"")) {
                argument = argument.substring(1);
            }

            if (argument.endsWith("\"")) {
                argument = argument.substring(0, argument.length() - 1);
            }

            if (argument.isEmpty()) {
                throw new IllegalArgumentException("argument is empty");
            }

            String[] tokens = argument.split("&", 3);
            if (tokens.length != 3) {
                throw new IllegalArgumentException("argument = [" + argument + "] is invalid");
            }

            File jarFile = new File(tokens[0]);
            File simpleClientJarFile = new File(tokens[1]);
            String configuration = tokens[2];

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            try {
                Class.forName("io.prometheus.client.CollectorRegistry");
                //LOGGER.info("simpleclient library found in application classpath");
            } catch (ClassNotFoundException e) {
                classLoader = new ChildFirstClassLoader(new URL[]{simpleClientJarFile.toURI().toURL()}, classLoader);
                //LOGGER.info("simpleclient library not found in application classpath");
                //LOGGER.info("injecting metrics-exporter-simpleclient");
                //LOGGER.info("NOTE: if your application requires the simpleclient library, you will see a ClassNotFoundException");
            }

            classLoader = new ChildFirstClassLoader(new URL[]{jarFile.toURI().toURL()}, classLoader);

            Class clazz = classLoader.loadClass("org.devopology.metrics.exporter.MetricsExporter");
            Object object = clazz.getDeclaredConstructor((Class[]) null).newInstance((Object[]) null);
            Method start = clazz.getDeclaredMethod("start", new Class[]{String.class});
            start.invoke(object, new Object[] { configuration });

            LOGGER.info("running");
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("exiting");
            System.exit(1);
        } catch (Exception e) {
            Throwable t = null;

            if (e instanceof InvocationTargetException) {
                t = e.getCause();
            } else {
                t = e;
            }

            // Special case for a ConfigurationException, since we can't have a code dependency on metrics-exporter
            if (t.getClass().getName().equals("org.devopology.metrics.exporter.common.converter.ConverterException")) {
                LOGGER.error("configuration error");
                LOGGER.error(t.getMessage());
            } else {
                t.printStackTrace();
            }

            LOGGER.error("exiting");
            System.exit(1);
        }
    }
}
