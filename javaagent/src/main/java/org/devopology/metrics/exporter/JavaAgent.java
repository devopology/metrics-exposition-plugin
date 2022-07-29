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

import org.devopology.common.classloader.JarClassLoader;
import org.devopology.common.jar.Jar;
import org.devopology.common.jar.BytesJarEntry;
import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;
import org.devopology.common.precondition.Precondition;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.JarInputStream;

/**
 * Class to implement a pluggable javaagent
 */
@SuppressWarnings("unchecked")
public class JavaAgent {

    private static final String PACKAGE_NAME;

    static {
        String classname = JavaAgent.class.getName();
        PACKAGE_NAME = classname.substring(0, classname.lastIndexOf("."));
    }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PACKAGE_NAME);

    private static final String IO_PROMETHEUS_CLIENT_COLLECTOR_REGISTRY_CLASSNAME = "io.prometheus.client.CollectorRegistry";

    // Embedded simpleclient package (jar)
    private static final String SIMPLECLIENT_PKG = "simpleclient.pkg";

    // Embedded exporter package (jar)
    private static final String EXPORTER_PKG = "exporter.pkg";

    private ExporterProxy exporterProxy;

    /**
     * Premain javaagent method
     *
     * @param arguments
     * @param instrumentation
     * @throws Exception
     */
    public static void premain(String arguments, Instrumentation instrumentation) throws Exception {
        new JavaAgent().start(arguments);
    }

    /**
     * Method to start the agent
     *
     * @param argument
     */
    public void start(String argument) {
        // argument is ignored

        try {
            LOGGER.info("starting: " + Version.getVersion());

            Arguments arguments = Arguments.create();

            File agentJarFile = arguments.getJavaAgentJarFile();
            LOGGER.trace(String.format("agent jar = [%s]", agentJarFile));

            // Load the agent jar
            Jar argentJar = new Jar();
            argentJar.load(agentJarFile);

            // Load the embedded jars
            LOGGER.trace(String.format("resolving [%s]", SIMPLECLIENT_PKG));
            BytesJarEntry simpleClientJarEntry = argentJar.get(SIMPLECLIENT_PKG);

            LOGGER.trace(String.format("resolving [%s]", EXPORTER_PKG));
            BytesJarEntry exporterJarEntry = argentJar.get(EXPORTER_PKG);

            // Validate the agent jar has an embedded "simpleclient.pkg"
            Precondition.checkState(
                    simpleClientJarEntry != null,
                    String.format("failed to resolve [%s] in [%s]", SIMPLECLIENT_PKG, agentJarFile));

            // Validate the agent jar has an embedded "exporter.pkg"
            Precondition.checkState(
                    exporterJarEntry != null,
                    String.format("failed to resolve [%s] in [%s]", EXPORTER_PKG, agentJarFile));

            // Load the simpleclient jar
            Jar simpleClientJar = new Jar();
            simpleClientJar.load(new JarInputStream(simpleClientJarEntry.getInputStream()));

            // Load the exporter jar
            Jar exporterJar = new Jar();
            exporterJar.load(new JarInputStream(exporterJarEntry.getInputStream()));

            // Get the current classloader
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // Try to load the CollectorRegistry class
            try {
                classLoader.loadClass(IO_PROMETHEUS_CLIENT_COLLECTOR_REGISTRY_CLASSNAME);
            } catch (ClassNotFoundException e) {
                // The CollectorRegistry class wasn't found
                // Hook in the classloader for the embedded simpleclient classes

                LOGGER.info(String.format("simpleclient libraries not found, using embedded simplelclient libaries"));
                LOGGER.info(String.format("NOTE: if your application requires the simpleclient libraries, you will see a ClassNotFoundException"));

                classLoader = new JarClassLoader(simpleClientJar, classLoader);
                classLoader.loadClass(IO_PROMETHEUS_CLIENT_COLLECTOR_REGISTRY_CLASSNAME);
            }

            // Hook in the classloader for the embedded exporter classes
            classLoader = new JarClassLoader(exporterJar, classLoader);

            // Start the Exporter via the proxy
            this.exporterProxy = new ExporterProxy(classLoader);
            this.exporterProxy.start(arguments.getYamlConfigurationFile());

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
