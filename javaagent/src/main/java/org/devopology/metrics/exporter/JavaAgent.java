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

import org.devopology.common.classloader.ChildFirstJarClassLoader;
import org.devopology.common.jar.Jar;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaAgent.class);

    private static final String IO_PROMETHEUS_CLIENT_COLLECTOR_REGISTRY_CLASSNAME = "io.prometheus.client.CollectorRegistry";
    private static final String ORG_DEVOPOLOGY_METRICS_EXPORTER_COMMON_CONVERTER_CONVERTER_EXCEPTION = "org.devopology.metrics.exporter.common.converter.ConverterException";

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
            LOGGER.info(String.format("%s", Version.getVersion()));

            JavaAgentArguments javaAgentArguments = JavaAgentArguments.create();

            File agentJarFile = javaAgentArguments.getJavaAgentJarFile();
            LOGGER.trace(String.format("agent jar = [%s]", agentJarFile));

            // Load the agent jar
            Jar argentJar = new Jar();
            argentJar.load(agentJarFile);

            // Load the embedded simpleclient package
            LOGGER.trace(String.format("resolving [%s]", SIMPLECLIENT_PKG));

            // Check that the agent jar contains the simpleclient package
            Precondition.isTrue(
                    argentJar.containsKey(SIMPLECLIENT_PKG),
                    String.format("failed to resolve [%s] in [%s]", SIMPLECLIENT_PKG, agentJarFile));

            // Load the embedded simpleclient package
            Jar simpleClientJar = new Jar();
            simpleClientJar.load(new JarInputStream(argentJar.get(SIMPLECLIENT_PKG).getInputStream()));

            LOGGER.trace(String.format("resolving [%s]", EXPORTER_PKG));

            // Check that the agent jar contains the exporter package
            Precondition.isTrue(argentJar.containsKey(
                    EXPORTER_PKG),
                    String.format("failed to resolve [%s] in [%s]", EXPORTER_PKG, agentJarFile));

            // Load the embedded exporter package
            Jar exporterJar = new Jar();
            exporterJar.load(new JarInputStream(argentJar.get(EXPORTER_PKG).getInputStream()));

            /*
             * Set up the classloader chaining for isolation
             *
             * parent classloader
             *   ApplicationClassLoader (agent classes)
             *     ChildFirstJarClassLoader (simpleclient classes) (optional)
             *       ChildFirstJarClassLoader (exporter classes)
             */

            // Get the current classloader
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // Try to load the CollectorRegistry class
            try {
                classLoader.loadClass(IO_PROMETHEUS_CLIENT_COLLECTOR_REGISTRY_CLASSNAME);
            } catch (ClassNotFoundException e) {
                // The CollectorRegistry class wasn't found
                LOGGER.info(String.format("simpleclient libraries not found, using embedded simplelclient libraries"));
                LOGGER.info(String.format("NOTE: if your application requires the simpleclient libraries, you will see a ClassNotFoundException"));

                // Add the classloader for the embedded simpleclient classes
                classLoader = new ChildFirstJarClassLoader(simpleClientJar, classLoader);
                classLoader.loadClass(IO_PROMETHEUS_CLIENT_COLLECTOR_REGISTRY_CLASSNAME);
            }

            // Added classloader for the embedded exporter classes
            classLoader = new ChildFirstJarClassLoader(exporterJar, classLoader);

            // Start the Exporter via reflection using the proxy
            exporterProxy = new ExporterProxy(classLoader);
            exporterProxy.start(javaAgentArguments.getYamlConfigurationFile());

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
            if (t.getClass().getName().equals(ORG_DEVOPOLOGY_METRICS_EXPORTER_COMMON_CONVERTER_CONVERTER_EXCEPTION)) {
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
