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
