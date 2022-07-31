/*
 * Copyright 2022 Douglas Hoard
 *
 * Licensed under the Apache License, org.devopology.metrics.exporter.Version 2.0 (the "License");
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

import io.prometheus.client.Info;
import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;
import org.devopology.common.precondition.Precondition;

import java.io.File;

/**
 * Class to implement a simple test application that creates a single Gauge and increments it
 */
public class Standalone {

    private static final String METRICS_EXPORTER_STANDALONE = "metrics_exporter_standalone_version";
    private static final String METRICS_EXPORTER_STANDALONE_HELP = "metrics-exporter-standalone version";
    private static final String VERSION = "version";

    private static final Logger LOGGER = LoggerFactory.getLogger(Standalone.class);

    public static void main(String[] args) throws Exception {
        new Standalone().run(args);
    }

    private void run(String[] args) throws Exception {
        Precondition.notNull(args, "args is null");
        Precondition.isTrue(args.length > 0, IllegalArgumentException.class, "exporter.yaml argument required");

        try {
            LOGGER.info(String.format("%s", Version.getVersion()));
            LOGGER.warn("EXPERIMENTAL");
            LOGGER.info(String.format("YAML configuration file [%s]", args[0]));

            File yamlConfigurationFile = new File(args[0]);

            Precondition.exists(yamlConfigurationFile, String.format("file [%s] doesn't exist", yamlConfigurationFile));
            Precondition.exists(yamlConfigurationFile, String.format("file [%s] isn't a file", yamlConfigurationFile));
            Precondition.exists(yamlConfigurationFile, String.format("file [%s] isn't readable", yamlConfigurationFile));

            new Exporter().start(yamlConfigurationFile);

            Info info = Info.build().name(METRICS_EXPORTER_STANDALONE).help(METRICS_EXPORTER_STANDALONE_HELP).register();
            info.info(VERSION, Version.getVersion());

            LOGGER.info("running");

            Thread.sleep(0);
        } catch (InterruptedException e) {
            // DO NOTHING
        }
    }
}
