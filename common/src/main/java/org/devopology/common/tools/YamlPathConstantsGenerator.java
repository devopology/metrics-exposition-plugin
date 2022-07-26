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

package org.devopology.common.tools;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class YamlPathConstantsGenerator {

    public static void main(String[] args) throws Exception {
        args = new String[1];
        args[0] = "configuration/exporter.yml";
        new YamlPathConstantsGenerator().execute(args);
    }

    private void execute(String[] args) throws Exception {
        // Load the YAML configuration using a custom Resolver to return String values
        Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new LoaderOptions(), new org.yaml.snakeyaml.resolver.Resolver() {
            protected void addImplicitResolvers() {
                this.addImplicitResolver(Tag.MERGE, MERGE, "<");
                this.addImplicitResolver(Tag.YAML, YAML, "!&*");
            }
        });

        List<String> list = new ArrayList<>();
        Map<String, Object> yamlMap = yaml.load(new FileReader(args[0]));
        traverse("", yamlMap, list);

        Collections.sort(list);

        for (String string : list) {
            System.out.println(string);
        }
    }

    private void traverse(String path, Object object, List<String> list) {
        if (object instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) object;
            Set<String> mapKeySet = map.keySet();
            for (String key : mapKeySet) {
                traverse(path + "." + key, map.get(key), list);
            }
        }

        if (object instanceof String) {
            String name = path.substring(1)
                    .replaceAll(Pattern.quote("."), "_")
                    .toUpperCase();

            String value = "$" + path;

            list.add(String.format("public static final String %s_PATH = \"%s\";", name, value));
        }
    }
}

