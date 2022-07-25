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

package org.devopology.common.converter;

import org.devopology.common.precondition.Precondition;
import org.devopology.common.type.Type;

import java.io.File;

/**
 *
 */
public class ReadableFileConverter implements Converter<File> {

    public File convert(Object value, Required required, String description) throws ConverterException {
        Precondition.notNull(description, "description is null");
        Precondition.notEmpty(description, "description is empty");

        if (value == null) {
            if (required == Converter.Required.TRUE) {
                throw new ConverterException(String.format("%s is null", description));
            } else {
                return null;
            }
        }

        if (Type.isType(String.class, value)) {
            String string = (String) value;
            File file = new File(string);

            if (!file.exists()) {
                throw new ConverterException(String.format("%s = [%s] file doesn't exist", description, value));
            }

            if (!file.isFile()) {
                throw new ConverterException(String.format("%s = [%s] isn't a file", description, value));
            }

            if (!file.canRead()) {
                throw new ConverterException(String.format("%s = [%s] file isn't a readable", description, value));
            }

            return file;
        }

        throw new ConverterException(String.format("path [%s] = [%s] isn't a string", description, value));
    }
}
