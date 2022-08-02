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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Class to dispatch requests to handlers
 */
public class DispatcherHttpHandler implements HttpHandler {

    private HttpHandler INTERNAL_SERVER_ERROR_HTTP_HANDLER = new InternalServerErrorHttpHandler();

    private HttpHandler defaultHttpHandler;
    private List<DispatcherMapping> dispatcherMappingList;

    /**
     * Constructor
     */
    public DispatcherHttpHandler() {
        dispatcherMappingList = new ArrayList<>();
    }

    /**
     * Method to set the default HttPHandler
     *
     * @param httpHandler
     */
    public void setDefaultHttpHandler(HttpHandler httpHandler) {
        this.defaultHttpHandler = httpHandler;
    }

    /**
     * Method to add a MatchingHttpHandler. The default HttpHandler should not be added.
     *
     * @param predicate
     * @param httpHandler
     */
    public void addHttpHandler(Predicate<HttpServerExchange> predicate, HttpHandler httpHandler) {
        dispatcherMappingList.add(new DispatcherMapping(predicate, httpHandler));
    }

    /**
     * Method to add a MatchingHttpHandler. The default HttpHandler should not be added.
     *
     * @param dispatcherMapping
     */
    public void addHttpHandler(DispatcherMapping dispatcherMapping) {
        Precondition.notNull(dispatcherMapping, "dispatcherMapping is null");

        dispatcherMappingList.add(dispatcherMapping);
    }


    /**
     * Method to handle the HttpServerExchange
     *
     * @param httpServerExchange
     * @throws Exception
     */
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        // Add headers to prevent caching
        httpServerExchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "no-cache");
        httpServerExchange.getResponseHeaders().put(Headers.EXPIRES, "0");
        httpServerExchange.getResponseHeaders().put(Headers.PRAGMA, "no-cache");

        try {
            // Dispatch the httpServerExchange
            for (DispatcherMapping dispatcherMapping : dispatcherMappingList) {
                if (dispatcherMapping.getPredicate().test(httpServerExchange)) {
                    dispatcherMapping.getHttpHandler().handleRequest(httpServerExchange);
                    return;
                }
            }

            defaultHttpHandler.handleRequest(httpServerExchange);
        } catch (Exception e) {
            INTERNAL_SERVER_ERROR_HTTP_HANDLER.handleRequest(httpServerExchange);
        }
    }

}
