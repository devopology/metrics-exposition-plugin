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

import org.devopology.common.precondition.Precondition;;

/**
 * Method to convert an Object to a Long
 */
@SuppressWarnings("unchecked")
public class LongConverter implements Converter<Long> {

    @Override
    public Long convert(Object object) throws ConverterException {
        Precondition.notNull(object, "object is null");

        if (Long.class.isInstance(object)) {
            return (Long) object;
        }

        if (String.class.isInstance(object)) {
            String string = (String) object;

            try {
                return Long.parseLong(string);
            } catch (NumberFormatException e) {
                throw new ConverterException(String.format("object value [%s] can't be converted to an Long", string));
            }
        }

        throw new ConverterException(String.format("object class [%s] isn't a Long", object.getClass()));
    }
}
