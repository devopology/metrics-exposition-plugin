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

package org.devopology.common.password;

import org.devopology.common.precondition.Precondition;
import org.devopology.common.sha.SHA;

/**
 * Class to implement an encrypted password
 * <p>
 * The encrypted password algorithm, SHA-512(salt:password) isn't
 * considered secure and could be cracked by Hashcat
 * but works for this scenario
 * <p>
 * https://hashcat.net/hashcat/
 */
public class HashedPassword {

    private static final String SHA_512 = "SHA512";
    private static final int MINIMUM_SALT_LENGTH = 32;

    private String salt;
    private String hashedPassword;

    /**
     * Constructor
     *
     * @param salt
     * @param password
     */
    public HashedPassword(String salt, String password) {
        Precondition.notNull(salt, "salt is null");
        Precondition.notEmpty(salt, "salt is empty");
        Precondition.notNull(password, "password is null");
        Precondition.notEmpty(password, "password is empty");

        salt = salt.trim();

        if (salt.length() < MINIMUM_SALT_LENGTH) {
            throw new IllegalArgumentException(
                    String.format(
                            "salt is too short [%d], minimum of [%d] characters required",
                            salt.length(),
                            MINIMUM_SALT_LENGTH));
        }

        this.salt = salt;

        password = password.trim();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SHA_512);
        stringBuilder.append(":");
        stringBuilder.append(salt);
        stringBuilder.append(":");
        stringBuilder.append(SHA.sha512Hash(salt + ":" + password));

        hashedPassword = stringBuilder.toString();
    }

    /**
     * Constructor
     *
     * @param hashedPassword
     */
    public HashedPassword(String hashedPassword) {
        Precondition.notNull(hashedPassword, "hashedPassword is null");
        Precondition.notEmpty(hashedPassword, "hashedPassword is empty");

        hashedPassword = hashedPassword.trim();

        String[] tokens = hashedPassword.split(":");

        if (tokens.length != 3) {
            throw new IllegalArgumentException(String.format("invalid hashed password format"));
        }

        if (!SHA_512.equals(tokens[0])) {
            throw new IllegalArgumentException(String.format("invalid hashed password format"));
        }

        if (tokens[1].length() != MINIMUM_SALT_LENGTH) {
            throw new IllegalArgumentException(String.format("invalid hashed password format"));
        }

        this.salt = tokens[1];
        this.hashedPassword = hashedPassword;
    }

    /**
     * Method to get the salt
     *
     * @return
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Method to get the encrypted password
     *
     * @return
     */
    public String getHashedPassword() {
        return hashedPassword;
    }

    /**
     * Method to compare whether the encrypted password is equal another Object
     *
     * @param object
     * @return
     */
    @Override
    public boolean equals(Object object) {
        if (HashedPassword.class.isInstance(object)) {
            HashedPassword hashedPassword = (HashedPassword) object;
            return this.hashedPassword.equals(hashedPassword.hashedPassword);
        }

        return false;
    }
}
