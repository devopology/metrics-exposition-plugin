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

package org.devopology.metrics.exporter.common.sha;

import org.devopology.common.sha1.SHA1;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SHA1Test {

    @Test
    public void test1() {
        String string = "password";
        String expectedSHA1 = "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8";
        String actualSHA1 = SHA1.hash(string);

        assertEquals(expectedSHA1, actualSHA1);
    }

    @Test
    public void test2() {
        String salt = "ctDGDprJW56vzoK";
        String string = "password";
        String expectedSHA1 = "e8664fe821ad2bdf3d88c2ba2b726c0ce0181daa";
        String actualSHA1 = SHA1.hash(salt + "/" + string);

        assertEquals(expectedSHA1, actualSHA1);
    }
}
