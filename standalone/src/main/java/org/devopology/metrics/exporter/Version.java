package org.devopology.metrics.exporter;/*
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

import java.io.InputStreamReader;
import java.util.Properties;

public class Version {

    private Version() {
        // DO NOTHING
    }

    public synchronized static String getVersion() {
        String version = "unknown";

        try {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(Version.class.getResourceAsStream("/standalone.properties"), "UTF-8"));
            if (properties.containsKey("version")) {
                version = properties.getProperty("version");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return version;
    }
}
