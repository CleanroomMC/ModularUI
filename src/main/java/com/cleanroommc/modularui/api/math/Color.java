package com.cleanroommc.modularui.api.math;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;

public class Color implements Iterable<Integer> {

    /**
     * Creates a color int. All values should be 0 - 255
     */
    public static int rgb(int red, int green, int blue) {
        return argb(red, green, blue, 255);
    }

    /**
     * Creates a color int. All values should be 0 - 255
     */
    public static int argb(int red, int green, int blue, int alpha) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | ((blue & 0xFF));
    }

    /**
     * Creates a color int. All values should be 0 - 1
     */
    public static int argb(float red, float green, float blue, float alpha) {
        return argb((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
    }

    /**
     * Creates a color int. All values should be 0 - 255
     */
    public static int rgba(int red, int green, int blue, int alpha) {
        return ((red & 0xFF) << 24) | ((green & 0xFF) << 16) | ((blue & 0xFF) << 8) | (alpha & 0xFF);
    }

    /**
     * Creates a color int. All values should be 0 - 1
     */
    public static int rgba(float red, float green, float blue, float alpha) {
        return rgba((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
    }

    /**
     * Creates a color int. All values should be 0 - 1
     */
    public static int rgb(float red, float green, float blue) {
        return argb(red, green, blue, 1f);
    }

    /**
     * Converts a HSV color to rgba
     *
     * @param hue        value from 0 to 360
     * @param saturation value from 0 to 1
     * @param value      value from 0 to 1
     * @param alpha      value from 0 to 1
     * @return the color
     */
    public static int ofHSV(int hue, float saturation, float value, float alpha) {
        hue = Math.max(0, Math.min(hue, 360));

        float c = value * saturation;
        float x = c * (1 - (((hue / 60f) % 2) - 1));
        x = Math.max(x, -x);
        float m = value - c;
        float r, g, b;
        if (hue < 60) {
            r = c;
            g = x;
            b = 0;
        } else if (hue < 120) {
            r = x;
            g = c;
            b = 0;
        } else if (hue < 180) {
            r = 0;
            g = c;
            b = x;
        } else if (hue < 240) {
            r = 0;
            g = x;
            b = c;
        } else if (hue < 300) {
            r = x;
            g = 0;
            b = c;
        } else {
            r = c;
            g = 0;
            b = x;
        }
        return argb(r + m, g + m, b + m, alpha);
    }

    public static int withRed(int color, int red) {
        color &= ~(0xFF << 16);
        return color | red << 16;
    }

    public static int withGreen(int color, int green) {
        color &= ~(0xFF << 8);
        return color | green << 8;
    }

    public static int withBlue(int color, int blue) {
        color &= ~0xFF;
        return color | blue;
    }

    public static int withAlpha(int color, int alpha) {
        color &= ~(0xFF << 24);
        return color | alpha << 24;
    }

    public static int withRed(int color, float red) {
        return withRed(color, (int) (red * 255));
    }

    public static int withGreen(int color, float green) {
        return withGreen(color, (int) (green * 255));
    }

    public static int withBlue(int color, float blue) {
        return withBlue(color, (int) (blue * 255));
    }

    public static int withAlpha(int color, float alpha) {
        return withAlpha(color, (int) (alpha * 255));
    }

    /**
     * @return the red value
     */
    public static int getRed(int rgba) {
        return rgba >> 16 & 255;
    }

    /**
     * @return the green value
     */
    public static int getGreen(int rgba) {
        return rgba >> 8 & 255;
    }

    /**
     * @return the blue value
     */
    public static int getBlue(int rgba) {
        return rgba & 255;
    }

    /**
     * @return the alpha value
     */
    public static int getAlpha(int rgba) {
        return rgba >> 24 & 255;
    }

    /**
     * @return the red value
     */
    public static float getRedF(int rgba) {
        return getRed(rgba) / 255f;
    }

    /**
     * @return the green value
     */
    public static float getGreenF(int rgba) {
        return getGreen(rgba) / 255f;
    }

    /**
     * @return the blue value
     */
    public static float getBlueF(int rgba) {
        return getBlue(rgba) / 255f;
    }

    /**
     * @return the alpha value
     */
    public static float getAlphaF(int rgba) {
        return getAlpha(rgba) / 255f;
    }

    /**
     * @return rgba as an array [red, green, blue, alpha]
     */
    public static int[] getValues(int rgba) {
        return new int[]{getRed(rgba), getGreen(rgba), getBlue(rgba), getAlpha(rgba)};
    }

    public static int rgbaToArgb(int rgba) {
        return Color.argb(getAlpha(rgba), getRed(rgba), getGreen(rgba), getBlue(rgba));
    }

    public static int argbToRgba(int argb) {
        return Color.rgba(getRed(argb), getGreen(argb), getBlue(argb), getAlpha(argb));
    }

    public static int invert(int rgb) {
        int alpha = Color.getAlpha(rgb);
        if (alpha == 0) {
            alpha = 255;
        }
        return Color.argb(255 - getRed(rgb), 255 - getGreen(rgb), 255 - getBlue(rgb), alpha);
    }

    public static int average(int... colors) {
        int r = 0, g = 0, b = 0, a = 0;
        for (int color : colors) {
            r += getRed(color);
            g += getGreen(color);
            b += getBlue(color);
            a += getAlpha(color);
        }
        return argb(r / colors.length, g / colors.length, b / colors.length, a / colors.length);
    }

    public static int interpolate(int color1, int color2, double value) {
        value = MathHelper.clamp(value, 0, 1);
        int r = (int) ((Color.getRed(color2) - Color.getRed(color1)) * value + Color.getRed(color1));
        int g = (int) ((Color.getGreen(color2) - Color.getGreen(color1)) * value + Color.getGreen(color1));
        int b = (int) ((Color.getBlue(color2) - Color.getBlue(color1)) * value + Color.getBlue(color1));
        int a = (int) ((Color.getAlpha(color2) - Color.getAlpha(color1)) * value + Color.getAlpha(color1));
        return Color.argb(r, g, b, a);
    }

    @Nullable
    public static Integer ofJson(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            return jsonElement.getAsInt();
        }
        if (jsonElement.isJsonArray()) {
            return null;
        }
        if (jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            int red = JsonHelper.getInt(json, 255, "r", "red");
            int green = JsonHelper.getInt(json, 255, "g", "green");
            int blue = JsonHelper.getInt(json, 255, "b", "blue");
            int alpha = JsonHelper.getInt(json, 255, "a", "alpha");
            return Color.argb(red, green, blue, alpha);
        }
        String string = jsonElement.getAsString();
        if (string.startsWith("#")) {
            string = string.substring(1);
        } else if (string.startsWith("0x")) {
            string = string.substring(2);
        }
        try {
            return Integer.parseInt(string, 16);
        } catch (NumberFormatException e) {
            ModularUI.LOGGER.error("Error parsing json color {}", jsonElement);
        }
        return null;
    }

    public static final Color WHITE = new Color(rgb(255, 255, 255), new int[]{},
            rgb(230, 230, 230),
            rgb(205, 205, 205),
            rgb(180, 180, 180),
            rgb(155, 155, 155),
            rgb(130, 130, 130));

    public static final Color BLACK = new Color(rgb(0, 0, 0), new int[]{
            rgb(25, 25, 25),
            rgb(50, 50, 50),
            rgb(75, 75, 75),
            rgb(100, 100, 100),
            rgb(125, 125, 125)});

    // from here on color values are taken from https://github.com/flutter/flutter/blob/master/packages/flutter/lib/src/material/colors.dart

    public static final Color RED = new Color(0xFFF44336,
            new int[]{0xFFEF5350, 0xFFE57373, 0xFFEF9A9A, 0xFFFFCDD2, 0xFFFFEBEE},
            0xFFE53935, 0xFFD32F2F, 0xFFC62828, 0xFFB71C1C);

    public static final Color RED_ACCENT = new Color(0xFFFF5252, new int[]{0xFFFF8A80}, 0xFFFF1744, 0xFFD50000);

    public static final Color PINK = new Color(0xFFE91E63,
            new int[]{0xFFEC407A, 0xFFF06292, 0xFFF48FB1, 0xFFF8BBD0, 0xFFFCE4EC},
            0xFFD81B60, 0xFFC2185B, 0xFFAD1457, 0xFF880E4F);

    public static final Color PINK_ACCENT = new Color(0xFFFF4081, new int[]{0xFFFF80AB}, 0xFFF50057, 0xFFC51162);

    public static final Color PURPLE = new Color(0xFF9C27B0,
            new int[]{0xFFAB47BC, 0xFFBA68C8, 0xFFCE93D8, 0xFFE1BEE7, 0xFFF3E5F5},
            0xFF8E24AA, 0xFF7B1FA2, 0xFF6A1B9A, 0xFF4A148C);

    public static final Color PURPLE_ACCENT = new Color(0xFFE040FB, new int[]{0xFFEA80FC}, 0xFFD500F9, 0xFFAA00FF);

    public static final Color DEEP_PURPLE = new Color(0xFF673AB7,
            new int[]{0xFF7E57C2, 0xFF9575CD, 0xFFB39DDB, 0xFFD1C4E9, 0xFFEDE7F6},
            0xFF5E35B1, 0xFF512DA8, 0xFF4527A0, 0xFF311B92);

    public static final Color DEEP_PURPLE_ACCENT = new Color(0xFF7C4DFF, new int[]{0xFFB388FF}, 0xFF651FFF, 0xFF651FFF);

    public static final Color INDIGO = new Color(0xFF3F51B5,
            new int[]{0xFF5C6BC0, 0xFF7986CB, 0xFF9FA8DA, 0xFFC5CAE9, 0xFFE8EAF6},
            0xFF3949AB, 0xFF303F9F, 0xFF283593, 0xFF1A237E);

    public static final Color INDIGO_ACCENT = new Color(0xFF536DFE, new int[]{0xFF8C9EFF}, 0xFF3D5AFE, 0xFF304FFE);

    public static final Color BLUE = new Color(0xFF2196F3,
            new int[]{0xFF42A5F5, 0xFF64B5F6, 0xFF90CAF9, 0xFFBBDEFB, 0xFFE3F2FD},
            0xFF1E88E5, 0xFF1976D2, 0xFF1565C0, 0xFF0D47A1);

    public static final Color BLUE_ACCENT = new Color(0xFF448AFF, new int[]{0xFF82B1FF}, 0xFF2979FF, 0xFF2962FF);

    public static final Color LIGHT_BLUE = new Color(0xFF03A9F4,
            new int[]{0xFF29B6F6, 0xFF4FC3F7, 0xFF81D4FA, 0xFFB3E5FC, 0xFFE1F5FE},
            0xFF039BE5, 0xFF0288D1, 0xFF0277BD, 0xFF01579B);

    public static final Color LIGHT_BLUE_ACCENT = new Color(0xFF40C4FF, new int[]{0xFF80D8FF}, 0xFF00B0FF, 0xFF0091EA);

    public static final Color CYAN = new Color(0xFF00BCD4,
            new int[]{0xFF26C6DA, 0xFF4DD0E1, 0xFF80DEEA, 0xFFB2EBF2, 0xFFE0F7FA},
            0xFF00ACC1, 0xFF0097A7, 0xFF00838F, 0xFF006064);

    public static final Color CYAN_ACCENT = new Color(0xFF18FFFF, new int[]{0xFF84FFFF}, 0xFF00E5FF, 0xFF00B8D4);

    public static final Color TEAL = new Color(0xFF009688,
            new int[]{0xFF26A69A, 0xFF4DB6AC, 0xFF80CBC4, 0xFFB2DFDB, 0xFFE0F2F1},
            0xFF00897B, 0xFF00796B, 0xFF00695C, 0xFF004D40);

    public static final Color TEAL_ACCENT = new Color(0xFF64FFDA, new int[]{0xFFA7FFEB}, 0xFF1DE9B6, 0xFF00BFA5);

    public static final Color GREEN = new Color(0xFF4CAF50,
            new int[]{0xFF66BB6A, 0xFF81C784, 0xFFA5D6A7, 0xFFC8E6C9, 0xFFE8F5E9},
            0xFF43A047, 0xFF388E3C, 0xFF2E7D32, 0xFF1B5E20);

    public static final Color GREEN_ACCENT = new Color(0xFF69F0AE, new int[]{0xFFB9F6CA}, 0xFF00E676, 0xFF00C853);

    public static final Color LIGHT_GREEN = new Color(0xFF8BC34A,
            new int[]{0xFF9CCC65, 0xFFAED581, 0xFFC5E1A5, 0xFFDCEDC8, 0xFFF1F8E9},
            0xFF7CB342, 0xFF689F38, 0xFF558B2F, 0xFF33691E);

    public static final Color LIGHT_GREEN_ACCENT = new Color(0xFFB2FF59, new int[]{0xFFCCFF90}, 0xFF76FF03, 0xFF64DD17);

    public static final Color LIME = new Color(0xFFCDDC39,
            new int[]{0xFFD4E157, 0xFFDCE775, 0xFFE6EE9C, 0xFFF0F4C3, 0xFFF9FBE7},
            0xFFC0CA33, 0xFFAFB42B, 0xFF9E9D24, 0xFF827717);

    public static final Color LIME_ACCENT = new Color(0xFFEEFF41, new int[]{0xFFF4FF81}, 0xFFC6FF00, 0xFFAEEA00);

    public static final Color YELLOW = new Color(0xFFFFEB3B,
            new int[]{0xFFFFEE58, 0xFFFFF176, 0xFFFFF59D, 0xFFFFF9C4, 0xFFFFFDE7},
            0xFFFDD835, 0xFFFBC02D, 0xFFF9A825, 0xFFF57F17);

    public static final Color YELLOW_ACCENT = new Color(0xFFFFFF00, new int[]{0xFFFFFF8D}, 0xFFFFEA00, 0xFFFFD600);

    public static final Color AMBER = new Color(0xFFFFC107,
            new int[]{0xFFFFCA28, 0xFFFFD54F, 0xFFFFE082, 0xFFFFECB3, 0xFFFFF8E1},
            0xFFFFB300, 0xFFFFA000, 0xFFFF8F00, 0xFFFF6F00);

    public static final Color AMBER_ACCENT = new Color(0xFFFFD740, new int[]{0xFFFFE57F}, 0xFFFFC400, 0xFFFFAB00);

    public static final Color ORANGE = new Color(0xFFFF9800,
            new int[]{0xFFFFA726, 0xFFFFB74D, 0xFFFFCC80, 0xFFFFE0B2, 0xFFFFF3E0},
            0xFFFB8C00, 0xFFF57C00, 0xFFEF6C00, 0xFFE65100);

    public static final Color ORANGE_ACCENT = new Color(0xFFFFAB40, new int[]{0xFFFFD180}, 0xFFFF9100, 0xFFFF6D00);

    public static final Color DEEP_ORANGE = new Color(0xFFFF5722,
            new int[]{0xFFFF7043, 0xFFFF8A65, 0xFFFFAB91, 0xFFFFCCBC, 0xFFFBE9E7},
            0xFFF4511E, 0xFFE64A19, 0xFFD84315, 0xFFBF360C);

    public static final Color DEEP_ORANGE_ACCENT = new Color(0xFFFF6E40, new int[]{0xFFFF9E80}, 0xFFFF3D00, 0xFFDD2C00);

    public static final Color BROWN = new Color(0xFF795548,
            new int[]{0xFF8D6E63, 0xFFA1887F, 0xFFBCAAA4, 0xFFD7CCC8, 0xFFEFEBE9},
            0xFF6D4C41, 0xFF5D4037, 0xFF4E342E, 0xFF3E2723);

    public static final Color GREY = new Color(0xFF9E9E9E,
            new int[]{0xFFBDBDBD, 0xFFE0E0E0, 0xFFEEEEEE, 0xFFF5F5F5, 0xFFFAFAFA},
            0xFF757575, 0xFF616161, 0xFF424242, 0xFF212121);

    public static final Color BLUE_GREY = new Color(0xFF607D8B,
            new int[]{0xFF78909C, 0xFF90A4AE, 0xFFB0BEC5, 0xFFCFD8DC, 0xFFECEFF1},
            0xFF546E7A, 0xFF455A64, 0xFF37474F, 0xFF263238);

    public final int normal;
    private final int[] shadeBright;
    private final int[] shadeDark;
    private final int[] all;

    public Color(int normal, int[] shadeBright, int... shadeDark) {
        this.normal = normal;
        this.shadeBright = shadeBright;
        this.shadeDark = shadeDark;
        this.all = new int[shadeBright.length + shadeDark.length + 1];
        int index = 0;
        for (int i = shadeBright.length - 1; i >= 0; i--) {
            all[index++] = shadeBright[i];
        }
        all[index++] = normal;
        for (int shade : shadeDark) {
            all[index++] = shade;
        }
    }

    public int bright(int index) {
        return shadeBright[index];
    }

    public int dark(int index) {
        return shadeDark[index];
    }

    @NotNull
    @Override
    public Iterator<Integer> iterator() {
        return Arrays.stream(all).iterator();
    }
}
