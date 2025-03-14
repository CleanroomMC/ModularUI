package com.cleanroommc.modularui;

import com.cleanroommc.modularui.utils.NumberFormat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormatTest {

    // non localized separator for unified test results
    private static final NumberFormat.Params defaultParams = NumberFormat.DEFAULT.copyToBuilder().decimalSeparator('.').build();
    private static NumberFormat.Params formatParams = defaultParams;

    @Test
    void testDefaultFormatting() {
        formatParams = defaultParams;

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
        // TODO I actually expect this to result in 1.235m (since the 5 at the end would be rounding up)
        test("1.234m", 0.0012345);
        test("123.5µ", 0.00012345);
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
    }
}
