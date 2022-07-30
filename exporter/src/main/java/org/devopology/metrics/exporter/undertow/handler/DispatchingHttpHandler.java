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
 * Class to dispatch requests to handlers
 */
public class DispatchingHttpHandler implements HttpHandler {

    private Boolean isCachingEnabled;
    private Long cacheMilliseconds;

    private HttpHandler healthyHttpHandler;
    private HttpHandler faviconHttpHandler;
    private HttpHandler defaultHttpHandler;

    public DispatchingHttpHandler(Boolean isCachingEnabled, Long cacheMilliseconds) {
        Precondition.notNull(isCachingEnabled, "isCachingEnabled is null");

        if (isCachingEnabled) {
            Precondition.notNull(cacheMilliseconds, "cacheMilliseconds is null");
            Precondition.inRange(cacheMilliseconds, 1, Long.MAX_VALUE, "cacheMilliseconds is outside range (1 - " + Long.MAX_VALUE + ")");
        }

        this.isCachingEnabled = isCachingEnabled;
        this.cacheMilliseconds = cacheMilliseconds;
        healthyHttpHandler = new HealthyHttpHandler();
        faviconHttpHandler = new FaviconHttpHandler();
        defaultHttpHandler = new MetricsHttpHandler(isCachingEnabled, cacheMilliseconds);
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        // Add headers to prevent caching
        httpServerExchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "no-cache");
        httpServerExchange.getResponseHeaders().put(Headers.EXPIRES, "0");
        httpServerExchange.getResponseHeaders().put(Headers.PRAGMA, "no-cache");

        // Dispatch the httpServerExchange
        String requestPath = httpServerExchange.getRequestPath();
        if (requestPath.startsWith("/-/healthy")) {
            healthyHttpHandler.handleRequest(httpServerExchange);
        } else if (requestPath.equals("/favicon.ico")) {
            faviconHttpHandler.handleRequest(httpServerExchange);
        } else {
            defaultHttpHandler.handleRequest(httpServerExchange);
        }
    }
}
