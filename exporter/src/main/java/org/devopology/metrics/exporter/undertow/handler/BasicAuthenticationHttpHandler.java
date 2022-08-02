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

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.devopology.common.precondition.Precondition;

import java.util.Collections;
import java.util.List;

/**
 * Class to implement BASIC authentication
 */
public class BasicAuthenticationHttpHandler implements HttpHandler {

    private HttpHandler httpHandler;

    /**
     * Constructor
     *
     * @param identityManager
     * @param httpHandler
     */
    public BasicAuthenticationHttpHandler(IdentityManager identityManager, HttpHandler httpHandler) {
        Precondition.notNull(identityManager, "identityManager is null");
        Precondition.notNull(httpHandler, "httpHandler is null");

        httpHandler = new AuthenticationCallHandler(httpHandler);
        httpHandler = new AuthenticationConstraintHandler(httpHandler);
        List<AuthenticationMechanism> mechanisms = Collections.<AuthenticationMechanism>singletonList(new BasicAuthenticationMechanism("/"));
        httpHandler = new AuthenticationMechanismsHandler(httpHandler, mechanisms);
        this.httpHandler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, httpHandler);
    }

    /**
     * Method to handle the HttpServerExchange
     *
     * @param httpServerExchange
     * @throws Exception
     */
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        httpHandler.handleRequest(httpServerExchange);
    }
}
