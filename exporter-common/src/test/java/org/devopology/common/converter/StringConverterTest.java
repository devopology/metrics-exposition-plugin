package org.devopology.common.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringConverterTest {

    @Test
    public void testValidConversion() throws ConverterException {
        Object object = "string";
        String expected = (String) object;
        String actual = Converter.STRING.convert(object);

        assertEquals(expected, actual);
    }

    @Test
    public void testInvalidConversion1() throws ConverterException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Converter.STRING.convert(null);
        });
    }

    @Test
    public void testInvalidConversion2() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.STRING.convert(new Object());
        });
    }
}
