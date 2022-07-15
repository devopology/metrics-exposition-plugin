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

package org.devopology.metrics.exposition.plugin;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to implement a simple username / password IdentityManager
 */
public class SimpleIdentityManager implements IdentityManager {

    private SimpleAccount simpleAccount;

    /**
     * Constructor
     *
     * @param username
     * @param password
     */
    public SimpleIdentityManager(String username, String password) {
        if (username == null) {
            throw new IllegalArgumentException("username is null");
        }
        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("username is empty");
        }
        if (password == null) {
            throw new IllegalArgumentException("password is null");
        }
        if (password.trim().isEmpty()) {
            throw new IllegalArgumentException("password is empty");
        }
        this.simpleAccount = new SimpleAccount(username, password);
    }

    /**
     * Method to verify an Account
     *
     * @param account
     * @return
     */
    @Override
    public Account verify(Account account) {
        return account;
    }

    /**
     * Method to verify an id and Credential
     *
     * @param id
     * @param credential
     * @return
     */
    @Override
    public Account verify(String id, Credential credential) {
        if (credential instanceof PasswordCredential) {
            char[] password = ((PasswordCredential) credential).getPassword();
            return this.simpleAccount.verify(password);
        }
        return null;
    }

    /**
     * Method to verify a Credential
     *
     * @param credential
     * @return
     */
    @Override
    public Account verify(Credential credential) {
        return null;
    }

    private class SimpleAccount implements Account {

        private Principal principal;
        private char[] password;
        private Set<String> rolesSet;

        public SimpleAccount(String username, String password) {
            this.rolesSet = new HashSet<>();
            this.principal = new SimplePrincipal(username);
            this.password = password.toCharArray();
        }

        @Override
        public Principal getPrincipal() {
            return this.principal;
        }

        @Override
        public Set<String> getRoles() {
            return this.rolesSet;
        }

        public SimpleAccount verify(char[] password) {
            if (Arrays.equals(this.password, password)) {
                return this;
            }

            return null;
        }
    }

    private class SimplePrincipal implements Principal {

        private String username;

        public SimplePrincipal(String username) {
            this.username = username;
        }

        @Override
        public String getName() {
            return this.username;
        }
    }
}
