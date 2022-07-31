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

package org.devopology.common.classloader;

import org.devopology.common.jar.Jar;
import org.devopology.common.precondition.Precondition;

import java.io.IOException;

/**
 * Class to implement a child-first Jar classloader
 */
public class ChildFirstJarClassLoader extends ChildFirstJarInputStreamClassLoader {

    /**
     * Constructor
     *
     * @param jar
     * @param parent
     */
    public ChildFirstJarClassLoader(Jar jar, ClassLoader parent) throws IOException {
        super(parent);

        Precondition.notNull(jar, "jar is null");

        this.jar = jar;
    }
}
