package com.cleanroommc.modularui;

import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.utils.SIPrefix;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormatTest {

    // non localized separator for unified test results
    private static final NumberFormat.Params defaultParams = NumberFormat.DEFAULT.copyToBuilder().decimalSeparator('.').build();
    private static NumberFormat.Params formatParams = defaultParams;

    @Test
    void testSiPrefix() {
        test(0.0, SIPrefix.One);
        test(Double.NaN, SIPrefix.One);
        test(Double.POSITIVE_INFINITY, SIPrefix.Infinite);
        test(1.0, SIPrefix.One);
        test(10.0, SIPrefix.One);
        test(1000.0, SIPrefix.One);
        test(9_999.999999999, SIPrefix.One);
        test(10_000, SIPrefix.Kilo);
        test(123_456, SIPrefix.Kilo);
        test(1_234_567, SIPrefix.Mega);
        test(123_456_789_123_456_789.0, SIPrefix.Peta);
        test(123_456_789_123_456_789.2320340280257190234710932475, SIPrefix.Peta);
        test(0.999999999999999999999, SIPrefix.One);
        test(0.000_1, SIPrefix.Micro);
        test(0.000_000_000_000_000_069, SIPrefix.Atto);
        test(1000.000_000_000_000_000_069, SIPrefix.One);
    }

    private void test(double x, SIPrefix prefix) {
        assertEquals(NumberFormat.findBestPrefix(x), prefix);
        assertEquals(NumberFormat.findBestPrefix(-x), prefix);
    }

    @Test
    void testDefaultFormatting() {
        formatParams = defaultParams;

        test("0", 0.0);
        test("∞", Double.POSITIVE_INFINITY);
        test("-∞", Double.NEGATIVE_INFINITY);
        test("NaN", Double.NaN);

        test("1.235", 1.23456);
        test("12.35", 12.3456);
        test("123.5", 123.456);
        test("1234", 1234);

        test("12.3k", 12345);
        test("123k", 123456);
        test("1.23M", 1234567);
        test("123M", 123456789);

        test("123m", 0.12345);
        test("12.3m", 0.012345);
        test("1.23m", 0.0012345);
        test("123µ", 0.00012345);

        test("-123.5", -123.456);
        test("-1234", -1234);
        test("-12.3k", -12345);
        test("-123k", -123456);
        test("-1.23M", -1234567);
        test("-123M", -123456789);
        test("-123m", -0.12345);
        test("-12.3m", -0.012345);
    }

    @Test
    void testConsiderSuffixLengthFalse() {
        formatParams = defaultParams.copyToBuilder().considerSuffixForLength(false).build();

        test("1.235", 1.23456);
        test("12.35", 12.3456);
        test("123.5", 123.456);
        test("1234", 1234);

        test("12.35k", 12345);
        test("123.5k", 123456);
        test("1.235M", 1234567);
        test("123.5M", 123456789);

        test("123.5m", 0.12345);
        test("12.35m", 0.012345);
        // Doesn't properly round up due to floating point precision
        test("1.234m", 0.0012345);
        test("123.4µ", 0.00012345);
    }

    @Test
    void testConsiderDecimalSeparatorLengthTrue() {
        formatParams = defaultParams.copyToBuilder().considerDecimalSeparatorForLength(true).build();

        test("1.23", 1.23456);
        test("12.3", 12.3456);
        test("123", 123.456);
        test("1234", 1234);

        test("12k", 12345);
        test("123k", 123456);
        test("1.2M", 1234567);
        test("123M", 123456789);

        test("123m", 0.12345);
        test("12m", 0.012345);
        test("1.2m", 0.0012345);
        test("123µ", 0.00012345);
    }

    @Test
    void testConsiderOnlyDecimalsLengthTrue() {
        formatParams = defaultParams.copyToBuilder().considerOnlyDecimalsForLength(true).considerSuffixForLength(false).maxLength(2).build();

        test("1.23", 1.23456);
        test("12.35", 12.3456);
        test("123.46", 123.456);
        test("1234", 1234);

        test("12.35k", 12345);
        test("123.46k", 123456);
        test("1.23M", 1234567);
        test("123.46M", 123456789);

        test("123.45m", 0.12345);
        test("12.35m", 0.012345);
        test("1.23m", 0.0012345);
        test("123.45µ", 0.00012345);
    }

    private void test(String s, double value) {
        assertEquals(s, NumberFormat.format(value, formatParams));
        if (!Double.isInfinite(value) && !Double.isNaN(value)) {
            assertEquals(s, NumberFormat.format(new BigDecimal(value), formatParams));
        }
    }
}
