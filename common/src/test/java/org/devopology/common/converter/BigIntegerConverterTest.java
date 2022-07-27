package org.devopology.common.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigIntegerConverterTest {

    @Test
    public void testValidConversion1() throws ConverterException {
        Object object = "12345000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        BigInteger expected = new BigInteger((String) object);
        BigInteger actual = Converter.BIG_INTEGER.convert(object);

        assertEquals(expected, actual);
    }

    @Test
    public void testInvalidConversion1() throws ConverterException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Converter.BIG_INTEGER.convert(null);
        });
    }

    @Test
    public void testInvalidConversion2() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BIG_INTEGER.convert("");
        });
    }

    @Test
    public void testInvalidConversion3() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BIG_INTEGER.convert(" ");
        });
    }

    @Test
    public void testInvalidConversion4() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BIG_INTEGER.convert("12345x");
        });
    }

    @Test
    public void testInvalidConversion5() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BIG_INTEGER.convert("x12345");
        });
    }

    @Test
    public void testInvalidConversion6() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BIG_INTEGER.convert(new Object());
        });
    }
}
