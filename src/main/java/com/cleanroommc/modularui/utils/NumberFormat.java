package com.cleanroommc.modularui.utils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class NumberFormat {

    private static final NavigableMap<Double, String> suffixesByPower = new TreeMap<>();
    private static final java.text.NumberFormat[] NUMBER_FORMAT = {
            new DecimalFormat("0."),
            new DecimalFormat("0.#"),
            new DecimalFormat("0.##"),
            new DecimalFormat("0.###"),
            new DecimalFormat("0.####"),
            new DecimalFormat("0.#####"),
            new DecimalFormat("0.######"),
            new DecimalFormat("0.#######"),
            new DecimalFormat("0.########"),
            new DecimalFormat("0.#########"),
    };

    static {
        suffixesByPower.put(0.000_000_000_000_000_001D, "a");
        suffixesByPower.put(0.000_000_000_000_001D, "f");
        suffixesByPower.put(0.000_000_000_001D, "p");
        suffixesByPower.put(0.000_000_001D, "n");
        suffixesByPower.put(0.000_001D, "u");
        suffixesByPower.put(0.001D, "m");
        suffixesByPower.put(1_000D, "k");
        suffixesByPower.put(1_000_000D, "M");
        suffixesByPower.put(1_000_000_000D, "G");
        suffixesByPower.put(1_000_000000_000D, "T");
        suffixesByPower.put(1_000_000000_000_000D, "P");
        suffixesByPower.put(1_000_000000_000_000_000D, "E");
    }

    @NotNull
    public static String format(double value, int precision) {
        //Double.MIN_VALUE == -Double.MIN_VALUE so we need an adjustment here
        if (value == Double.MIN_VALUE) return format(Double.MIN_VALUE + 1, precision);
        if (value == 0) return "0";
        if (value < 0) return '-' + format(-value, precision);
        double divideBy;
        String suffix;
        if (value < pow(10, precision)) {
            divideBy = 1;
            suffix = "";
        } else {
            Map.Entry<Double, String> e = suffixesByPower.floorEntry(value);
            divideBy = e.getKey();
            suffix = e.getValue();
        }

        double truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10D) != (truncated / 10);
        return hasDecimal ? NUMBER_FORMAT[precision].format(truncated / 10D) + suffix : NUMBER_FORMAT[precision].format(truncated / 10) + suffix;
    }

    @NotNull
    public static String format(double value) {
        return format(value, 3);
    }

    private static int pow(int num, int e) {
        int result = num;
        for (int i = 0; i < e; i++) {
            result *= num;
        }
        return result;
    }
}
