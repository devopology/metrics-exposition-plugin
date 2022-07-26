package org.devopology.common.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatConverterTest {

    @Test
    public void testValidConversion1() throws ConverterException {
        Object object = "12345.0";
        Float expected = Float.parseFloat((String) object);
        Float actual = Converter.FLOAT.convert(object);

        assertEquals(expected, actual);
    }

    @Test
    public void testValidConversion2() throws ConverterException {
        Object object = "12345";
        Float expected = Float.parseFloat((String) object);
        Float actual = Converter.FLOAT.convert(object);

        assertEquals(expected, actual);
    }

    @Test
    public void testInvalidConversion1() throws ConverterException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Converter.FLOAT.convert(null);
        });
    }

    @Test
    public void testInvalidConversion2() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.FLOAT.convert("");
        });
    }

    @Test
    public void testInvalidConversion3() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.FLOAT.convert(" ");
        });
    }

    @Test
    public void testInvalidConversion4() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.FLOAT.convert("12345x");
        });
    }

    @Test
    public void testInvalidConversion5() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.FLOAT.convert("x12345");
        });
    }

    @Test
    public void testInvalidConversion6() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.FLOAT.convert(new Object());
        });
    }
}
