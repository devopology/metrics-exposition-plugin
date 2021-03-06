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

package org.devopology.metrics.exporter.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 * Class to handle an internal server error
 */
public class InternalServerErrorHttpHandler implements HttpHandler {

    /**
     * Method to handle the HttpServerExchange
     *
     * @param httpServerExchange
     * @throws Exception
     */
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) {
        try {
            httpServerExchange.setStatusCode(500);
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            httpServerExchange.getResponseSender().send("500 INTERNAL SERVER ERROR");
        } catch (Exception e) {
            // DO NOTHING
        }
    }
}
