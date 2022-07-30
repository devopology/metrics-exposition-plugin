package org.devopology.common.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigDecimalConverterTest {

    @Test
    public void testValidConversion1() throws ConverterException {
        Object object = "12345000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0";
        BigDecimal expected = new BigDecimal((String) object);
        BigDecimal actual = Converter.BIG_DECIMAL.convert(object);

        assertEquals(expected, actual);
    }

    @Test
    public void testValidConversion2() throws ConverterException {
        Object object = "12345000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        BigDecimal expected = new BigDecimal((String) object);
        BigDecimal actual = Converter.BIG_DECIMAL.convert(object);

        assertEquals(expected, actual);
    }

    @Test
    public void testInvalidConversion1() throws ConverterException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Converter.BIG_DECIMAL.convert(null);
        });
    }

    @Test
    public void testInvalidConversion2() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BIG_DECIMAL.convert("");
        });
    }

    @Test
    public void testInvalidConversion3() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BIG_DECIMAL.convert(" ");
        });
    }

    @Test
    public void testInvalidConversion4() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BIG_DECIMAL.convert("12345x");
        });
    }

    @Test
    public void testInvalidConversion5() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BIG_DECIMAL.convert("x12345");
        });
    }

    @Test
    public void testInvalidConversion6() throws ConverterException {
        Assertions.assertThrows(ConverterException.class, () -> {
            Converter.BIG_DECIMAL.convert(new Object());
        });
    }
}
