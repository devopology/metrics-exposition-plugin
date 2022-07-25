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

package org.devopology.common.resolver;

import org.devopology.common.precondition.Precondition;

import java.util.Map;

public class Resolver {

    public final static Resolver OBJECT = new Resolver();

    public Object resolve(String path, Map<String, Object> map) throws ResolverException {
        Precondition.notNull(path, "path is null");
        Precondition.notEmpty(path, "path is empty");
        Precondition.notNull(map, "map is null");

        if ((path == null) || path.equals("/")) {
            throw new ResolverException("TODO invalid path");
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        Map<String, Object> currentMap = map;

        String[] keyTokens = path.split("/");
        for (int i = 0; i < keyTokens.length - 1; i++) {
            currentMap = (Map<String, Object>) currentMap.get(keyTokens[i]);
            if (currentMap == null) {
                return null;
            }
        }

        return currentMap.get(keyTokens[keyTokens.length - 1]);
    }
}
