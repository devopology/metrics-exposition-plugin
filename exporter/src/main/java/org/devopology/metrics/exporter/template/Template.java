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

package org.devopology.metrics.exporter.template;

import org.apache.commons.text.StringSubstitutor;
import org.devopology.common.precondition.Precondition;

import java.util.Map;

/**
 * Class to implement a Template
 */
public class Template {

    private String content;

    /**
     * Constructor
     *
     * @param content
     */
    public Template(String content) {
        Precondition.notNull(content, "content is null");
        Precondition.notEmpty(content, "content is empty");

        this.content = content.trim();
    }

    /**
     * Method to merge a Map of values with the Template resulting in a String
     *
     * @param valuesMap
     * @return
     */
    public String merge(Map<String, String> valuesMap) {
        return new StringSubstitutor(valuesMap).replace(content);
    }
}
