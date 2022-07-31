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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Class to implement an obfuscated password
 * <p>
 * Obfuscation is NOT encryption
 */
public class ObfuscatedPassword {

    private static final String BASE_64 = "BASE64";
    private static final int MINIMUM_SALT_LENGTH = 32;

    private String salt;
    private String obfuscatedPassword;

    /**
     * Constructor
     *
     * @param salt
     * @param password
     */
    public ObfuscatedPassword(String salt, String password) {
        Precondition.notNull(salt, "salt is null");
        Precondition.notEmpty(salt, "salt is empty");
        Precondition.notNull(password, "password is null");
        Precondition.notNull(password, "password is empty");

        salt = salt.trim();
        password = password.trim();

        if (salt.length() < MINIMUM_SALT_LENGTH) {
            throw new IllegalArgumentException(
                    String.format(
                            "salt is too short [%d], minimum of [%d] characters required",
                            salt.length(),
                            MINIMUM_SALT_LENGTH));
        }

        this.salt = salt;

        byte[] bytes = (salt + ":" + password).getBytes(StandardCharsets.UTF_8);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BASE_64);
        stringBuilder.append(":");
        stringBuilder.append(Base64.getEncoder().encodeToString(bytes));

        obfuscatedPassword = stringBuilder.toString();
    }

    /**
     * Constructor
     *
     * @param obfuscatedPassword
     */
    public ObfuscatedPassword(String obfuscatedPassword) {
        Precondition.notNull(obfuscatedPassword, "obfuscatedPassword is null");
        Precondition.notEmpty(obfuscatedPassword, "obfuscatedPassword is empty");

        obfuscatedPassword = obfuscatedPassword.trim();

        String[] tokens = obfuscatedPassword.split(":");

        if (tokens.length != 2) {
            throw new IllegalArgumentException(String.format("invalid obfuscated password format"));
        }

        if (!BASE_64.equals(tokens[0])) {
            throw new IllegalArgumentException(String.format("invalid obfuscated password format"));
        }

        byte[] bytes = Base64.getDecoder().decode(tokens[1]);
        String string = new String(bytes, StandardCharsets.UTF_8);

        this.salt = string.substring(0, string.indexOf(":"));
        this.obfuscatedPassword = obfuscatedPassword;
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
     * Method to get the obfuscated password
     *
     * @return
     */
    public String getObfuscatedPassword() {
        return obfuscatedPassword;
    }

    /**
     * Method to get the unobfuscated password
     *
     * @return
     */
    public String getUnobfuscatedPassword() {
        String[] tokens = obfuscatedPassword.split(":");
        byte[] bytes = Base64.getDecoder().decode(tokens[1]);
        String string = new String(bytes, StandardCharsets.UTF_8);
        String unobfuscatedPassword = string.substring(string.indexOf(":") + 1);
        return unobfuscatedPassword;
    }

    /**
     * Method to compare whether the obfuscated password is equal another Object
     *
     * @param object
     * @return
     */
    @Override
    public boolean equals(Object object) {
        if (ObfuscatedPassword.class.isInstance(object)) {
            ObfuscatedPassword obfuscatedPassword = (ObfuscatedPassword) object;
            return this.obfuscatedPassword.equals(obfuscatedPassword.obfuscatedPassword);
        }

        return false;
    }
}
