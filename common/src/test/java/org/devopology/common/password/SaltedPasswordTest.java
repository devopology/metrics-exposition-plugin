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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SaltedPasswordTest {

    @Test
    public void testSaltedPasswordIsValid() {
        String password = "password";
        String saltedPassword = "kKagpEG3h82BvZQt/3c91164992fb7fc0787fe4088a4963b52f95e392";

        assertTrue(SaltedPassword.isValid(saltedPassword, password));
    }

    @Test
    public void testSaltedPasswordIsInvalid1() {
        String password = "Password";
        String saltedPassword = "kKagpEG3h82BvZQt/3c91164992fb7fc0787fe4088a4963b52f95e392";

        assertFalse(SaltedPassword.isValid(saltedPassword, password));
    }

    @Test
    public void testSaltedPasswordIsInvalid2() {
        String password = "password";
        String saltedPassword = "/3c91164992fb7fc0787fe4088a4963b52f95e392";

        assertFalse(SaltedPassword.isValid(saltedPassword, password));
    }

    @Test
    public void testSaltedPasswordIsInvalid3() {
        String password = "password";
        String saltedPassword = "3c91164992fb7fc0787fe4088a4963b52f95e392";

        assertFalse(SaltedPassword.isValid(saltedPassword, password));
    }

    @Test
    public void testSaltedPasswordIsInvalid4() {
        String password = "password";
        String saltedPassword = "kKagpEG3h82BvZQt";

        assertFalse(SaltedPassword.isValid(saltedPassword, password));
    }

    @Test
    public void testSaltedPasswordIsInvalid5() {
        String password = "password";
        String saltedPassword = "kKagpEG3h82BvZQt/";

        assertFalse(SaltedPassword.isValid(saltedPassword, password));
    }

    @Test
    public void testSaltedPasswordIsInvalid6() {
        String password = null;
        String saltedPassword = "kKagpEG3h82BvZQt/";

        assertFalse(SaltedPassword.isValid(saltedPassword, password));
    }

    @Test
    public void testSaltedPasswordIsInvalid7() {
        String password = "";
        String saltedPassword = "kKagpEG3h82BvZQt/";

        assertFalse(SaltedPassword.isValid(saltedPassword, password));
    }
}
