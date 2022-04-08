package com.cleanroommc.modularui.api;

import java.text.DecimalFormat;

public class NumberFormat {

    // kilo, Mega, Giga, Tera, Peta, Exa, Zetta, Yotta
    private static final String ABOVE_1_PREFIX = "kMGTPEZY";
    // millie, micro, nano, pico, femto, atto, zepto, yocto
    private static final String BELOW_1_PREFIX = "munpfazy";

    public static final DecimalFormat FORMAT_0 = new DecimalFormat("0.");
    public static final DecimalFormat FORMAT_1 = new DecimalFormat("0.#");
    public static final DecimalFormat FORMAT_2 = new DecimalFormat("0.##");
    public static final DecimalFormat FORMAT_3 = new DecimalFormat("0.###");

    public static String format(double num, java.text.NumberFormat format) {
        if (num == 0) {
            return "0";
        }
        if ((1 <= num && num < 1000)) {
            return format.format(num);
        }
        int index = 0;
        if ((num < 1 && num > 0) || (num < 0 && num > -1)) {
            while ((num > 0 && num <= 0.00095) || (num < 0 && num >= -0.00095)) {
                num *= 1000;
                if (++index == 7) {
                    break;
                }
            }
            return format.format(num * 1000) + BELOW_1_PREFIX.charAt(index);
        }
        while (num <= -999_950 || num >= 999_950) {
            num /= 1000;
            if (++index == 7) {
                break;
            }
        }
        return format.format(num / 1000) + ABOVE_1_PREFIX.charAt(index);
    }
}
