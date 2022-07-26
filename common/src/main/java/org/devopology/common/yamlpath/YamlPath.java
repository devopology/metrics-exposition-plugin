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

package org.devopology.common.yamlpath;

import com.google.gson.Gson;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.devopology.common.precondition.Precondition;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Class to implement YamlPath access. Similar to JsonPath
 */
public class YamlPath {

    private DocumentContext documentContext;

    /**
     * Constructor
     *
     * @param documentContext
     */
    private YamlPath(DocumentContext documentContext) {
        this.documentContext = documentContext;
    }

    /**
     * Method to read an Object based on a path
     *
     * @param path
     * @return
     * @throws PathNotFoundException
     */
    public Object read(String path) throws PathNotFoundException {
        return read(path, true);
    }

    /**
     * Method to read an Object based on a path
     *
     * @param path
     * @return
     * @throws PathNotFoundException
     */
    public Object read(String path, boolean isRequired) throws PathNotFoundException {
        Precondition.notNull(path, "path is null");
        Precondition.notEmpty(path, "path is empty");

        try {
            return this.documentContext.read(path);
        } catch (com.jayway.jsonpath.PathNotFoundException e) {
            if (isRequired) {
                throw new PathNotFoundException(path, String.format("path [%s] not found", path));
            }
        }

        return null;
    }

    /**
     *
     * @param reader
     * @throws IOException
     */
    public static YamlPath parse(Reader reader) throws IOException {
        // Create a Yaml object that loads values (String, Integer, Boolean, timestamp) as Strings
        Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new LoaderOptions(), new org.yaml.snakeyaml.resolver.Resolver() {
            protected void addImplicitResolvers() {
                this.addImplicitResolver(Tag.MERGE, MERGE, "<");
                this.addImplicitResolver(Tag.YAML, YAML, "!&*");
            }
        });

        // Load the YAML file
        Map<String, Object> yamlMap = yaml.load(reader);

        // Convert the YAML to JSON
        StringWriter stringWriter = new StringWriter();
        new Gson().toJson(yamlMap, stringWriter);

        // Parse the JSON into a DocumentContext
        DocumentContext documentContext = JsonPath.parse(stringWriter.toString());

        // Created and return a new YamlPath
        return new YamlPath(documentContext);
    }
}
