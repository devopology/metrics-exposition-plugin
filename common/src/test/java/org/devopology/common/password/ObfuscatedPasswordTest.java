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

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObfuscatedPasswordTest {

    private static final Random RANDOM = new SecureRandom();

    @Test
    public void test() {
        String salt = randomString(32);
        String password = "password";

        ObfuscatedPassword obfuscatedPassword = new ObfuscatedPassword(salt, password);

        assertEquals(salt, obfuscatedPassword.getSalt());
        assertEquals(password, obfuscatedPassword.getUnobfuscatedPassword());

        ObfuscatedPassword obfuscatedPassword2 = new ObfuscatedPassword(obfuscatedPassword.getObfuscatedPassword());

        assertEquals(obfuscatedPassword.getSalt(), obfuscatedPassword2.getSalt());
        assertEquals(password, obfuscatedPassword2.getUnobfuscatedPassword());

        assertEquals(obfuscatedPassword.getObfuscatedPassword(), obfuscatedPassword2.getObfuscatedPassword());
    }

    private String randomString(int length) {
        int leftLimit = 48;
        int rightLimit = 122;

        return RANDOM.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
