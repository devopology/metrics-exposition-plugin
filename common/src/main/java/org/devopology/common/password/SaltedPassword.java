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
import org.devopology.common.sha1.SHA1;

public class SaltedPassword {

    private SaltedPassword() {
        // DO NOTHING
    }

    public static boolean isValid(String saltedPassword, String password) {
        Precondition.notNull(saltedPassword, "saltedPassword is null");
        Precondition.notEmpty(saltedPassword, "saltedPassword is empty");

        if (password == null) {
            return false;
        }

        password = password.trim();

        if (password.isEmpty()) {
            return false;
        }

        saltedPassword = saltedPassword.trim();

        if (saltedPassword.indexOf("/") < 1) {
            return false;
        }

        String salt = saltedPassword.substring(0, saltedPassword.indexOf("/"));
        String generatedSaltedPassword = null;

        if (salt != null)  {
            generatedSaltedPassword = salt + "/" + SHA1.hash(salt + "/" + password);
        }

        return saltedPassword.equals(generatedSaltedPassword);
    }
}
