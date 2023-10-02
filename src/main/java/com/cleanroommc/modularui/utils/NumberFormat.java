package com.cleanroommc.modularui.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;

public class NumberFormat {

    public static final DecimalFormat FORMAT = new DecimalFormat("0.###");

    private static final double[] FACTORS_HIGH = {1e3, 1e6, 1e9, 1e12, 1e15, 1e18};
    private static final double[] FACTORS_LOW = {1e-3, 1e-6, 1e-9, 1e-12, 1e-15, 1e-18};
    private static final String[] SUFFIX_HIGH = {"k", "M", "G", "T", "P", "E"};
    private static final String[] SUFFIX_LOW = {"m", "u", "n", "p", "f", "a"};

    public static String formatWithMaxDigits(double value) {
        return format(value, 4, true);
    }

    public static String formatWithMaxDigits(double value, int maxDigits) {
        return format(value, maxDigits, true);
    }

    public static String formatWithMaxDecimals(double value, int decimals) {
        return format(value, decimals, false);
    }

    private static String format(double value, int precision, boolean maxDigits) {
        if (value >= 1000) {
            int index;
            for (index = 0; index < 5; index++) {
                if (value < FACTORS_HIGH[index + 1]) {
                    break;
                }
            }
            return formatToString(value / FACTORS_HIGH[index], precision, maxDigits, SUFFIX_HIGH[index]);
        }
        if (value < 1) {
            int index;
            for (index = 0; index < 5; index++) {
                if (value >= FACTORS_LOW[index]) {
                    break;
                }
            }
            return formatToString(value / FACTORS_LOW[index], precision, maxDigits, SUFFIX_LOW[index]);
        }
        return formatToString(value, precision, maxDigits, StringUtils.EMPTY);
    }

    private static String formatToString(double value, int precision, boolean maxDigits, String suffix) {
        if (maxDigits) {
            String[] parts = String.valueOf(value).split("\\.");
            if (parts.length > 1) {
                precision -= parts[0].length();
            }
        }
        FORMAT.setMaximumFractionDigits(precision);
        return FORMAT.format(value) + suffix;
    }
}
