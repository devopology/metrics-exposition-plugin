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

public interface Converter<T> {

    BooleanConverter BOOLEAN = new BooleanConverter();
    IntegerConverter INTEGER = new IntegerConverter();
    LongConverter LONG = new LongConverter();
    FloatConverter FLOAT = new FloatConverter();
    DoubleConverter DOUBLE = new DoubleConverter();
    BigIntegerConverter BIG_INTEGER = new BigIntegerConverter();
    BigDecimalConverter BIG_DECIMAL = new BigDecimalConverter();
    StringConverter STRING = new StringConverter();
    HostOrIPAddressConverter HOST_OR_IP_ADDRESS = new HostOrIPAddressConverter();
    ReadableFileConverter READABLE_FILE = new ReadableFileConverter();
    ListConverter LIST_CONVERTER = new ListConverter();
    MapConverter MAP_CONVERTER = new MapConverter();

    <T> T convert(Object value) throws ConverterException;
}
