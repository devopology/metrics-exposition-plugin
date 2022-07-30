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

package org.devopology.exporter.common.converter;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import org.devopology.common.converter.ConverterException;
import org.devopology.common.converter.StringConverter;
import org.devopology.common.precondition.Precondition;

@SuppressWarnings("unchecked")
public class HostOrIPAddressConverter extends StringConverter {

    @Override
    public String convert(Object object) throws ConverterException {
        Precondition.notNull(object, "object is null");

        if (String.class.isInstance(object)) {
            String string = (String) object;
            boolean valid = false;

            if (InetAddresses.isInetAddress(string)) {
                valid = true;
            } else if (InternetDomainName.isValid(string)) {
                valid = true;
            }

            if (valid) {
                return string;
            }

            throw new ConverterException(String.format("object value [%s] isn't a domain name or IP address", string));
        }

        throw new ConverterException(String.format("object class [%s] isn't a String", object.getClass()));
    }
}