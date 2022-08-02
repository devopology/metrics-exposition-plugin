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

package org.devopology.metrics.exporter.resources;

import org.devopology.common.precondition.Precondition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Class to load resources
 */
public class Resources {

    /**
     * Method to get a resource as a byte array
     *
     * @param resource
     * @return
     * @throws IOException
     */
    public byte[] getResourceAsByteArray(String resource) throws IOException {
        Precondition.notNull(resource, "resource is null");
        Precondition.notEmpty(resource, "resource is empty");

        resource = resource.trim();

        try (InputStream inputStream = getResourceAsInputStream(resource)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            while (true) {
                int b = inputStream.read();
                if (b == -1) {
                    break;
                }

                byteArrayOutputStream.write((byte) b);
            }

            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * Method to get a resource as a String using UTF-8
     *
     * @param resource
     * @return
     * @throws IOException
     */
    public String getResourceAsString(String resource) throws IOException {
        Precondition.notNull(resource, "resource is null");
        Precondition.notEmpty(resource, "resource is empty");

        resource = resource.trim();

        return getResourceAsString(resource, StandardCharsets.UTF_8);
    }

    /**
     * Method to get a resource as a String using the specific Charset
     *
     * @param resource
     * @param charset
     * @return
     * @throws IOException
     */
    public String getResourceAsString(String resource, Charset charset) throws IOException {
        Precondition.notNull(resource, "resource is null");
        Precondition.notEmpty(resource, "resource is empty");
        Precondition.notNull(charset, "charset is null");

        resource = resource.trim();

        try (Reader reader = getResourceAsReader(resource, charset)) {
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                int c = reader.read();
                if (c == -1) {
                    break;
                }

                stringBuilder.append((char) c);
            }

            return stringBuilder.toString();
        }
    }

    /**
     * Method to get a resource as an InputStream
     *
     * @param resource
     * @return
     * @throws IOException
     */
    public InputStream getResourceAsInputStream(String resource) throws IOException {
        Precondition.notNull(resource, "resource is null");
        Precondition.notEmpty(resource, "resource is empty");

        resource = resource.trim();

        InputStream inputStream = getClass().getResourceAsStream(resource);
        if (inputStream == null) {
            throw new IOException(String.format("resource [%s] not found", resource));
        }

        return inputStream;
    }

    /**
     * Method to get a resource as Reader using UTF-8
     *
     * @param resource
     * @return
     * @throws IOException
     */
    public Reader getResourceAsReader(String resource) throws IOException {
        Precondition.notNull(resource, "resource is null");
        Precondition.notEmpty(resource, "resource is empty");

        resource = resource.trim();

        return getResourceAsReader(resource, StandardCharsets.UTF_8);
    }

    /**
     * Method to get a resource as a Reader using the specified Charset
     *
     * @param resource
     * @param charset
     * @return
     * @throws IOException
     */
    public Reader getResourceAsReader(String resource, Charset charset) throws IOException {
        Precondition.notNull(resource, "resource is null");
        Precondition.notEmpty(resource, "resource is empty");
        Precondition.notNull(charset, "charset is null");

        resource = resource.trim();

        return new InputStreamReader(getResourceAsInputStream(resource), charset);
    }
}
