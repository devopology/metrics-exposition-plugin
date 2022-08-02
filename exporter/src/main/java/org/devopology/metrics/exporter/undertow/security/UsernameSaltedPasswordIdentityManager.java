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

package org.devopology.metrics.exporter.undertow.security;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;
import org.devopology.common.logger.Logger;
import org.devopology.common.logger.LoggerFactory;
import org.devopology.common.password.HashedPassword;
import org.devopology.common.precondition.Precondition;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to implement a simple username / password IdentityManager
 */
public class UsernameSaltedPasswordIdentityManager implements IdentityManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsernameSaltedPasswordIdentityManager.class);

    private static final Set<String> ROLES = new HashSet<>();

    private String username;
    private HashedPassword hashedPassword;

    /**
     * Constructor
     *
     * @param username
     * @param encryptedPassword
     */
    public UsernameSaltedPasswordIdentityManager(String username, String encryptedPassword) {
        Precondition.notNull(username, "username is null");
        Precondition.notEmpty(username, "username is empty");
        Precondition.notNull(encryptedPassword, "encryptedPassword is null");
        Precondition.notEmpty(encryptedPassword, "encryptedPassword is empty");

        this.username = username.trim();
        this.hashedPassword = new HashedPassword(encryptedPassword);
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
     * @param username
     * @param credential
     * @return
     */
    @Override
    public Account verify(String username, Credential credential) {
        if (!this.username.equals(username)) {
            return null;
        }

        if (credential instanceof PasswordCredential) {
            char[] passwordCharacters = ((PasswordCredential) credential).getPassword();
            if (passwordCharacters != null) {
                String password = new String(passwordCharacters).trim();
                if (password.length() > 0) {
                    String salt = this.hashedPassword.getSalt();
                    HashedPassword hashedPassword = new HashedPassword(salt, password);
                    if (this.hashedPassword.equals(hashedPassword)) {
                        return new SimpleAccount(username, ROLES);
                    }
                }
            }
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

    /**
     * Class to implement a SimpleAccount
     */
    private class SimpleAccount implements Account {

        private Principal principal;
        private Set<String> rolesSet;

        public SimpleAccount(String username, Set<String> rolesSet) {
            principal = new SimplePrincipal(username);
            this.rolesSet = rolesSet;
        }

        @Override
        public Principal getPrincipal() {
            return principal;
        }

        @Override
        public Set<String> getRoles() {
            return rolesSet;
        }
    }

    /**
     * Class to implement a SimplePrincipal
     */
    private class SimplePrincipal implements Principal {

        private String username;

        public SimplePrincipal(String username) {
            this.username = username;
        }

        @Override
        public String getName() {
            return username;
        }
    }
}
