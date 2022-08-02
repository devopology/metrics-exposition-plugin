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
import org.devopology.common.precondition.Precondition;

import java.util.function.Predicate;

/**
 * Class to implement a DispatcherMapping
 */
public class DispatcherMapping {

    private Predicate<HttpServerExchange> predicate;
    private HttpHandler httpHandler;

    /**
     * Constructor
     *
     * @param predicate
     * @param httpHandler
     */
    public DispatcherMapping(Predicate<HttpServerExchange> predicate, HttpHandler httpHandler) {
        Precondition.notNull(predicate, "predicate is null");
        Precondition.notNull(httpHandler, "httpHandler is null");

        this.httpHandler = httpHandler;
        this.predicate = predicate;
    }

    /**
     * Method to get the mapping's HttpHandler
     *
     * @return
     */
    public HttpHandler getHttpHandler() {
        return httpHandler;
    }

    /**
     * Method to get the mapping's Predicate
     *
     * @return
     */
    public Predicate<HttpServerExchange> getPredicate() {
        return predicate;
    }
}
