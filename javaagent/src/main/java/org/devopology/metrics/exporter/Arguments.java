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
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Class to get javaagent arguments
 */
public class Arguments {

    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    private static final String ARGUMENT_DELIMITER_REGEX = "(=|:|&)";

    private File agentJarFile;
    private File yamlConfigurationFile;

    /**
     * Constructor
     */
    private Arguments() {
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
        return this.agentJarFile;
    }

    protected void setYamlConfigurationFile(File yamlConfigurationFile) {
        this.yamlConfigurationFile = yamlConfigurationFile;
    }

    /**
     * Method to get the YAML configuration File
     * @return
     */
    public File getYamlConfigurationFile() {
        return this.yamlConfigurationFile;
    }

    /**
     * Method to create arguments
     *
     * @return
     */
    public static Arguments create() {
        String javaIoTmpDir = System.getProperty(JAVA_IO_TMPDIR);

        try {
            File tempFile = File.createTempFile("test-", null);

            try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                fileOutputStream.write("test".getBytes(StandardCharsets.UTF_8));
            }

            tempFile.delete();
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("%s [%s] is required to be writable", JAVA_IO_TMPDIR, javaIoTmpDir));
        }

        Arguments arguments = new Arguments();

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> inputArgumentsList = runtimeMXBean.getInputArguments();
        for (String inputArgument : inputArgumentsList) {
            if (inputArgument.startsWith("-javaagent:")) {
                String [] tokens = inputArgument.split(ARGUMENT_DELIMITER_REGEX);
                arguments.setAgentJarFile(new File(tokens[1]));
                arguments.setYamlConfigurationFile(new File(tokens[tokens.length - 1]));
                return arguments;
            }
        }

        throw new RuntimeException(String.format("error getting -javaagent arguments"));
    }
}
