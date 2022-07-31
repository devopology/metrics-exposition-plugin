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

package org.devopology.common.sha;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SHATest {

    @Test
    public void test() {
        String value = "testing 1 2 3";
        String expectedSHA512Hash = "d9474a842bdcfde43e768f77a856559fb30d6dcb69f0c654ac1ca5a68cc7068e28a0bea7ac1983a347b024bc76a76ce5b8d843f4ac23275d81d33983f9afef41";

        String actualSHA512Hash = SHA.sha512Hash(value);

        assertEquals(expectedSHA512Hash, actualSHA512Hash);
    }
}
