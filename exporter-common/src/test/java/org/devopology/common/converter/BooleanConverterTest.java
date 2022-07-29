package org.devopology.common.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BooleanConverterTest {

    @Test
    public void testValidConversion1() throws ConverterException {
        Object object = "true";
        Boolean expected = Boolean.parseBoolean((String) object);
        Boolean actual = Converter.BOOLEAN.convert(object);

        assertEquals(expected, actual);
    }

    @Test
    public void testValidConversion2() throws ConverterException {
        Object object = "false";
        Boolean expected = Boolean.parseBoolean((String) object);
        Boolean actual = Converter.BOOLEAN.convert(object);

        assertEquals(expected, actual);
    }

    @Test
    public void testInvalidConversion1() throws ConverterException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Converter.BOOLEAN.convert(null);
        });
    }

    @Test
    public void testInvalidConversion2() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BOOLEAN.convert("");
        });
    }

    @Test
    public void testInvalidConversion3() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BOOLEAN.convert(" ");
        });
    }

    @Test
    public void testInvalidConversion4() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BOOLEAN.convert("12345x");
        });
    }

    @Test
    public void testInvalidConversion5() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BOOLEAN.convert("x12345");
        });
    }

    @Test
    public void testInvalidConversion6() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BOOLEAN.convert("FALSE");
        });
    }

    @Test
    public void testInvalidConversion7() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BOOLEAN.convert("TRUE");
        });
    }

    @Test
    public void testInvalidConversion8() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BOOLEAN.convert(new Object());
        });
    }

}
