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

package org.devopology.common.querystring;

import org.devopology.common.precondition.Precondition;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to parse a query string
 */
public class QueryString {

    /**
     * Constructor
     */
    private QueryString() {
        // DO NOTHING
    }

    /**
     * Method to parse a query string into a Map
     *
     * @param queryString
     * @return
     */
    public static Map<String, List<String>> parse(String queryString) {
        Precondition.notNull(queryString, "queryString is null");
        Precondition.notEmpty(queryString, "queryString is empty");

        queryString = queryString.trim();
        Map<String, List<String>> map = new HashMap<String, List<String>>();

        try {
            if (!queryString.isEmpty()) {
                String[] tokens = queryString.split("&");
                for (String token : tokens) {
                    String[] subTokens = token.split("=", 2);
                    if ((subTokens == null) || ("".equals(subTokens[0]))) {
                        continue;
                    }

                    String[] keyValuePair = new String[2];
                    keyValuePair[0] = URLDecoder.decode(subTokens[0], "UTF-8");

                    if (subTokens.length == 2) {
                        keyValuePair[1] = URLDecoder.decode(subTokens[1], "UTF-8");
                    } else {
                        keyValuePair[1] = "";
                    }

                    List<String> list = map.get(keyValuePair[0]);
                    if (list == null) {
                        list = new ArrayList<String>();
                        map.put(keyValuePair[0], list);
                    }

                    list.add(keyValuePair[1]);
                }

                //System.out.println(map);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            // Should never happen / UTF-8 is supported by all JVMs
        }

        return map;
    }
}
