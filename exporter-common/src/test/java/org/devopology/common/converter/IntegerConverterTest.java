package org.devopology.common.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegerConverterTest {

    @Test
    public void testValidConversion() throws ConverterException {
        Object object = "12345";
        Integer expected = Integer.parseInt((String) object);
        Integer actual = Converter.INTEGER.convert(object);

        assertEquals(expected, actual);
    }

    @Test
    public void testInvalidConversion1() throws ConverterException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Converter.INTEGER.convert(null);
        });
    }

    @Test
    public void testInvalidConversion2() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.INTEGER.convert("");
        });
    }

    @Test
    public void testInvalidConversion3() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.INTEGER.convert(" ");
        });
    }

    @Test
    public void testInvalidConversion4() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.INTEGER.convert("12345x");
        });
    }

    @Test
    public void testInvalidConversion5() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.INTEGER.convert("x12345");
        });
    }

    @Test
    public void testInvalidConversion6() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.INTEGER.convert(String.valueOf(Long.MAX_VALUE));
        });
    }

    @Test
    public void testInvalidConversion7() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.INTEGER.convert(new Object());
        });
    }
}
