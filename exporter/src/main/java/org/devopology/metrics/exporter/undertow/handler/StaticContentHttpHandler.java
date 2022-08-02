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
import org.devopology.common.precondition.Precondition;

/**
 * Class to implement a static content
 */
public class StaticContentHttpHandler implements HttpHandler {

    protected int statusCode;
    protected String contentType;
    protected String content;

    /**
     * Constructor
     *
     * @param statusCode
     * @param contentType
     * @param content
     */
    public StaticContentHttpHandler(int statusCode, String contentType, String content) {
        Precondition.inRange(statusCode, 100, 500, String.format("invalid status code [%d]", statusCode));
        Precondition.notNull(contentType, "contentType is null");
        Precondition.notEmpty(contentType, "contentType is empty");
        Precondition.notNull(content, "content is null");
        Precondition.notEmpty(content, "content is empty");

        this.statusCode = statusCode;
        this.contentType = contentType.trim();
        this.content = content.trim();
    }

    /**
     * Method to handle the HttpServerExchange
     *
     * @param httpServerExchange
     * @throws Exception
     */
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        httpServerExchange.setStatusCode(statusCode);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);
        httpServerExchange.getResponseSender().send(content);
    }
}
