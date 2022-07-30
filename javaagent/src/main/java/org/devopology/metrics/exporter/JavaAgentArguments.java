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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * Class to get javaagent arguments
 */
public class JavaAgentArguments {

    private static final String ARGUMENT_DELIMITER_REGEX = "(=|:|&)";

    private File agentJarFile;
    private File yamlConfigurationFile;

    /**
     * Constructor
     */
    private JavaAgentArguments() {
        // DO NOTHING
    }

    protected void setAgentJarFile(File agentJarFile) {
        this.agentJarFile = agentJarFile;
    }

    /**
     * Method to get the javagent jar File
     * @return
     */
    public File getJavaAgentJarFile() {
        return agentJarFile;
    }

    protected void setYamlConfigurationFile(File yamlConfigurationFile) {
        this.yamlConfigurationFile = yamlConfigurationFile;
    }

    /**
     * Method to get the YAML configuration File
     * @return
     */
    public File getYamlConfigurationFile() {
        return yamlConfigurationFile;
    }

    /**
     * Method to create arguments
     *
     * @return
     */
    public static JavaAgentArguments create() {
        JavaAgentArguments javaAgentArguments = new JavaAgentArguments();

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> inputArgumentsList = runtimeMXBean.getInputArguments();
        for (String inputArgument : inputArgumentsList) {
            if (inputArgument.startsWith("-javaagent:")) {
                String [] tokens = inputArgument.split(ARGUMENT_DELIMITER_REGEX);
                javaAgentArguments.setAgentJarFile(new File(tokens[1]));
                javaAgentArguments.setYamlConfigurationFile(new File(tokens[tokens.length - 1]));
                return javaAgentArguments;
            }
        }

        throw new RuntimeException(String.format("error getting -javaagent arguments"));
    }
}
