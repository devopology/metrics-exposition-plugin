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

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import net.jodah.expiringmap.ExpiringMap;
import org.devopology.common.precondition.Precondition;

import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class to handle all other requests not handled
 */
public class MetricsHttpHandler implements HttpHandler {

    private Boolean isCachingEnabled;
    private Long cacheMilliseconds;
    private Map<String, String> responseMap;

    public MetricsHttpHandler(Boolean isCachingEnabled, Long cacheMilliseconds) {
        Precondition.notNull(isCachingEnabled, "isCachingEnabled is null");

        if (isCachingEnabled) {
            Precondition.notNull(cacheMilliseconds, "cacheMilliseconds is null");
            Precondition.inRange(cacheMilliseconds, 1, Long.MAX_VALUE, "cacheMilliseconds is outside range (1 - " + Long.MAX_VALUE + ")");
        }

        this.isCachingEnabled = isCachingEnabled;
        this.cacheMilliseconds = cacheMilliseconds;

        if (isCachingEnabled) {
            responseMap = ExpiringMap.builder().expiration(cacheMilliseconds.longValue(), TimeUnit.MILLISECONDS).build();
        }
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        HeaderValues headerValues = httpServerExchange.getRequestHeaders().get(Headers.ACCEPT);

        String acceptHeader = null;
        if (headerValues != null) {
            acceptHeader = headerValues.get(0);
        }

        String contentType = TextFormat.chooseContentType(acceptHeader);
        httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);

        String response = null;

        if ((isCachingEnabled) && (cacheMilliseconds != null)) {
            synchronized (this) {
                response = responseMap.get(contentType);
                if (response == null) {
                    StringWriter stringWriter = new StringWriter(4096);
                    TextFormat.writeFormat(contentType, stringWriter, CollectorRegistry.defaultRegistry.metricFamilySamples());
                    response = stringWriter.toString();
                    responseMap.put(contentType, response);
                }
            }
        } else {
            StringWriter stringWriter = new StringWriter(4096);
            TextFormat.writeFormat(contentType, stringWriter, CollectorRegistry.defaultRegistry.metricFamilySamples());
            response = stringWriter.toString();
        }

        httpServerExchange.getResponseSender().send(response);
    }
}
