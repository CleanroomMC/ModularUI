package com.cleanroommc.modularui.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * A number formatter for large, small, positive, negative, integers and decimals with adjustable format parameters.
 * Test results can be seen at test/java/com.cleanroommc.modularui.FormatTest.
 */
public class NumberFormat {

    public static final Params DEFAULT = paramsBuilder()
            .roundingMode(RoundingMode.HALF_UP)
            .maxLength(4)
            .considerMinusForLength(false)
            .considerDecimalSeparatorForLength(false)
            .considerOnlyDecimalsForLength(false)
            .considerSuffixForLength(true)
            .build();

    public static final Params AMOUNT_TEXT = DEFAULT.copyToBuilder()
            .roundingMode(RoundingMode.DOWN)
            .build();

    private static final double[] FACTORS_HIGH = {1e3, 1e6, 1e9, 1e12, 1e15, 1e18, 1e21, 1e24};
    private static final double[] FACTORS_LOW = {1e-3, 1e-6, 1e-9, 1e-12, 1e-15, 1e-18, 1e-21, 1e-24};
    //private static final double[] ALL_FACTORS = new double[FACTORS_HIGH.length + FACTORS_LOW.length];
    private static final String[] SUFFIX_HIGH = {"k", "M", "G", "T", "P", "E", "Z", "Y"};
    private static final String[] SUFFIX_LOW = {"m", "µ", "n", "p", "f", "a", "z", "y"};
    private static final String NO_SUFFIX = "";

    /*static {
        for (int i = 0; i < FACTORS_LOW.length; i++) {
            ALL_FACTORS[i] = FACTORS_LOW[FACTORS_LOW.length - 1 - i];
        }
        System.arraycopy(FACTORS_HIGH, 0, ALL_FACTORS, FACTORS_LOW.length, FACTORS_HIGH.length);
    }*/

    public static Params params(DecimalFormat format, int maxLength, boolean considerOnlyDecimalsForLength, boolean considerDecimalSeparatorForLength, boolean considerMinusForLength, boolean considerSuffixForLength) {
        return new Params(format, maxLength, considerOnlyDecimalsForLength, considerDecimalSeparatorForLength, considerMinusForLength, considerSuffixForLength);
    }

    public static ParamsBuilder paramsBuilder() {
        return new ParamsBuilder();
    }

    public static class Params {
        public final DecimalFormat format;
        public final int maxLength;
        public final boolean considerOnlyDecimalsForLength;
        public final boolean considerDecimalSeparatorForLength;
        public final boolean considerMinusForLength;
        public final boolean considerSuffixForLength;

        public Params(DecimalFormat format, int maxLength, boolean considerOnlyDecimalsForLength, boolean considerDecimalSeparatorForLength, boolean considerMinusForLength, boolean considerSuffixForLength) {
            this.format = format;
            this.maxLength = maxLength;
            this.considerOnlyDecimalsForLength = considerOnlyDecimalsForLength;
            this.considerDecimalSeparatorForLength = considerDecimalSeparatorForLength;
            this.considerMinusForLength = considerMinusForLength;
            this.considerSuffixForLength = considerSuffixForLength;
            if (!this.considerOnlyDecimalsForLength && this.maxLength < 4) {
                throw new IllegalArgumentException("Max length must be at least 4 characters");
            }
        }

        public ParamsBuilder copyToBuilder() {
            DecimalFormat format = (DecimalFormat) this.format.clone();
            format.setMinimumFractionDigits(this.format.getMinimumFractionDigits());
            format.setMaximumFractionDigits(this.format.getMaximumFractionDigits());
            format.setMinimumIntegerDigits(this.format.getMinimumIntegerDigits());
            format.setMaximumIntegerDigits(this.format.getMaximumIntegerDigits());
            format.setRoundingMode(this.format.getRoundingMode());
            format.setGroupingSize(this.format.getGroupingSize());
            format.setGroupingUsed(this.format.isGroupingUsed());
            return new ParamsBuilder()
                    .format(format)
                    .maxLength(this.maxLength)
                    .considerOnlyDecimalsForLength(this.considerOnlyDecimalsForLength)
                    .considerDecimalSeparatorForLength(this.considerDecimalSeparatorForLength)
                    .considerMinusForLength(this.considerMinusForLength)
                    .considerSuffixForLength(this.considerSuffixForLength);
        }

        public String format(double number) {
            return NumberFormat.format(number, this);
        }
    }

    public static class ParamsBuilder {

        private DecimalFormat format;
        private int maxLength;
        private boolean considerOnlyDecimalsForLength;
        private boolean considerDecimalSeparatorForLength;
        private boolean considerMinusForLength;
        private boolean considerSuffixForLength;

        private DecimalFormat checkFormat() {
            if (this.format == null) {
                this.format = new DecimalFormat("0.###");
            }
            return this.format;
        }

        public ParamsBuilder format(DecimalFormat format) {
            this.format = format;
            return this;
        }

        public ParamsBuilder roundingMode(RoundingMode roundingMode) {
            checkFormat().setRoundingMode(roundingMode);
            return this;
        }

        public ParamsBuilder decimalFormatSymbols(DecimalFormatSymbols symbols) {
            checkFormat().setDecimalFormatSymbols(symbols);
            return this;
        }

        public ParamsBuilder decimalSeparator(char c) {
            DecimalFormatSymbols symbols = checkFormat().getDecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            checkFormat().setDecimalFormatSymbols(symbols);
            return this;
        }

        public ParamsBuilder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public ParamsBuilder considerOnlyDecimalsForLength(boolean considerOnlyDecimalsForLength) {
            this.considerOnlyDecimalsForLength = considerOnlyDecimalsForLength;
            return this;
        }

        public ParamsBuilder considerDecimalSeparatorForLength(boolean considerDecimalSeparatorForLength) {
            this.considerDecimalSeparatorForLength = considerDecimalSeparatorForLength;
            return this;
        }

        public ParamsBuilder considerMinusForLength(boolean considerMinusForLength) {
            this.considerMinusForLength = considerMinusForLength;
            return this;
        }

        public ParamsBuilder considerSuffixForLength(boolean considerSuffixForLength) {
            this.considerSuffixForLength = considerSuffixForLength;
            return this;
        }

        public Params build() {
            return new Params(checkFormat(), this.maxLength, this.considerOnlyDecimalsForLength, this.considerDecimalSeparatorForLength, this.considerMinusForLength, this.considerSuffixForLength);
        }
    }

    public static String format(double number, Params params) {
        boolean negative = number < 0;
        int maxLength = params.maxLength;
        if (negative) {
            number = -number;
            if (params.considerMinusForLength) maxLength--;
        }
        String formattedNumber = formatInternal(number, maxLength, params);
        if (negative) formattedNumber = "-" + formattedNumber;
        return formattedNumber;
    }

    private static String formatInternal(double number, int maxLength, Params params) {
        int n = FACTORS_HIGH.length - 1;
        String suffix = NO_SUFFIX;
        if (number > 9999) {
            int index;
            for (index = 0; index < n; index++) {
                if (number < FACTORS_HIGH[index + 1]) {
                    break;
                }
            }
            number /= FACTORS_HIGH[index];
            suffix = SUFFIX_HIGH[index];
        } else if (number < 1) {
            int index;
            for (index = 0; index < n; index++) {
                if (number >= FACTORS_LOW[index]) {
                    break;
                }
            }
            number /= FACTORS_LOW[index];
            suffix = SUFFIX_LOW[index];
        }
        return formatToString(number, suffix, maxLength, params);
    }

    private static String formatToString(double value, String suffix, int maxLength, Params params) {
        if (params.considerSuffixForLength && !suffix.isEmpty()) {
            maxLength -= suffix.length();
        }
        if (params.considerDecimalSeparatorForLength) {
            maxLength--;
        }
        if (!params.considerOnlyDecimalsForLength) {
            if (value % 1 > 0) {
                int intDigits = (int) (Math.log10(Math.floor(value)) + 1);
                maxLength -= intDigits;
            }
        }
        int m1 = params.format.getMaximumFractionDigits();
        int m2 = params.format.getMinimumFractionDigits();
        params.format.setMaximumFractionDigits(maxLength);
        String s = params.format.format(value) + suffix;
        params.format.setMaximumFractionDigits(m1);
        params.format.setMinimumFractionDigits(m2);
        return s;
    }
}
