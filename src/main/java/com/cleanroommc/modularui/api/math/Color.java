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

    public static final Color WHITE = new Color(0xFFFFFF, new int[]{},
            0xF7F7F7,
            0xEFEFEF,
            0xE7E7E7,
            0xDFDFDF,
            0xD7D7D7,
            0xCFCFCF,
            0xC7C7C7,
            0xBFBFBF);

    public static final Color BLACK = new Color(0x000000, new int[]{
            0x080808,
            0x101010,
            0x181818,
            0x202020,
            0x282828,
            0x303030,
            0x383838,
            0x404040
    });

    // from here on color values are taken from https://github.com/flutter/flutter/blob/master/packages/flutter/lib/src/material/colors.dart

    public static final Color RED = new Color(0xF44336,
            new int[]{0xEF5350, 0xE57373, 0xEF9A9A, 0xFFCDD2, 0xFFEBEE},
            0xE53935, 0xD32F2F, 0xC62828, 0xB71C1C);

    public static final Color RED_ACCENT = new Color(0xFF5252, new int[]{0xFF8A80}, 0xFF1744, 0xD50000);

    public static final Color PINK = new Color(0xE91E63,
            new int[]{0xEC407A, 0xF06292, 0xF48FB1, 0xF8BBD0, 0xFCE4EC},
            0xD81B60, 0xC2185B, 0xAD1457, 0x880E4F);

    public static final Color PINK_ACCENT = new Color(0xFF4081, new int[]{0xFF80AB}, 0xF50057, 0xC51162);

    public static final Color PURPLE = new Color(0x9C27B0,
            new int[]{0xAB47BC, 0xBA68C8, 0xCE93D8, 0xE1BEE7, 0xF3E5F5},
            0x8E24AA, 0x7B1FA2, 0x6A1B9A, 0x4A148C);

    public static final Color PURPLE_ACCENT = new Color(0xE040FB, new int[]{0xEA80FC}, 0xD500F9, 0xAA00FF);

    public static final Color DEEP_PURPLE = new Color(0x673AB7,
            new int[]{0x7E57C2, 0x9575CD, 0xB39DDB, 0xD1C4E9, 0xEDE7F6},
            0x5E35B1, 0x512DA8, 0x4527A0, 0x311B92);

    public static final Color DEEP_PURPLE_ACCENT = new Color(0x7C4DFF, new int[]{0xB388FF}, 0x651FFF, 0x651FFF);

    public static final Color INDIGO = new Color(0x3F51B5,
            new int[]{0x5C6BC0, 0x7986CB, 0x9FA8DA, 0xC5CAE9, 0xE8EAF6},
            0x3949AB, 0x303F9F, 0x283593, 0x1A237E);

    public static final Color INDIGO_ACCENT = new Color(0x536DFE, new int[]{0x8C9EFF}, 0x3D5AFE, 0x304FFE);

    public static final Color BLUE = new Color(0x2196F3,
            new int[]{0x42A5F5, 0x64B5F6, 0x90CAF9, 0xBBDEFB, 0xE3F2FD},
            0x1E88E5, 0x1976D2, 0x1565C0, 0x0D47A1);

    public static final Color BLUE_ACCENT = new Color(0x448AFF, new int[]{0x82B1FF}, 0x2979FF, 0x2962FF);

    public static final Color LIGHT_BLUE = new Color(0x03A9F4,
            new int[]{0x29B6F6, 0x4FC3F7, 0x81D4FA, 0xB3E5FC, 0xE1F5FE},
            0x039BE5, 0x0288D1, 0x0277BD, 0x01579B);

    public static final Color LIGHT_BLUE_ACCENT = new Color(0x40C4FF, new int[]{0x80D8FF}, 0x00B0FF, 0x0091EA);

    public static final Color CYAN = new Color(0x00BCD4,
            new int[]{0x26C6DA, 0x4DD0E1, 0x80DEEA, 0xB2EBF2, 0xE0F7FA},
            0x00ACC1, 0x0097A7, 0x00838F, 0x006064);

    public static final Color CYAN_ACCENT = new Color(0x18FFFF, new int[]{0x84FFFF}, 0x00E5FF, 0x00B8D4);

    public static final Color TEAL = new Color(0x009688,
            new int[]{0x26A69A, 0x4DB6AC, 0x80CBC4, 0xB2DFDB, 0xE0F2F1},
            0x00897B, 0x00796B, 0x00695C, 0x004D40);

    public static final Color TEAL_ACCENT = new Color(0x64FFDA, new int[]{0xA7FFEB}, 0x1DE9B6, 0x00BFA5);

    public static final Color GREEN = new Color(0x4CAF50,
            new int[]{0x66BB6A, 0x81C784, 0xA5D6A7, 0xC8E6C9, 0xE8F5E9},
            0x43A047, 0x388E3C, 0x2E7D32, 0x1B5E20);

    public static final Color GREEN_ACCENT = new Color(0x69F0AE, new int[]{0xB9F6CA}, 0x00E676, 0x00C853);

    public static final Color LIGHT_GREEN = new Color(0x8BC34A,
            new int[]{0x9CCC65, 0xAED581, 0xC5E1A5, 0xDCEDC8, 0xF1F8E9},
            0x7CB342, 0x689F38, 0x558B2F, 0x33691E);

    public static final Color LIGHT_GREEN_ACCENT = new Color(0xB2FF59, new int[]{0xCCFF90}, 0x76FF03, 0x64DD17);

    public static final Color LIME = new Color(0xCDDC39,
            new int[]{0xD4E157, 0xDCE775, 0xE6EE9C, 0xF0F4C3, 0xF9FBE7},
            0xC0CA33, 0xAFB42B, 0x9E9D24, 0x827717);

    public static final Color LIME_ACCENT = new Color(0xEEFF41, new int[]{0xF4FF81}, 0xC6FF00, 0xAEEA00);

    public static final Color YELLOW = new Color(0xFFEB3B,
            new int[]{0xFFEE58, 0xFFF176, 0xFFF59D, 0xFFF9C4, 0xFFFDE7},
            0xFDD835, 0xFBC02D, 0xF9A825, 0xF57F17);

    public static final Color YELLOW_ACCENT = new Color(0xFFFF00, new int[]{0xFFFF8D}, 0xFFEA00, 0xFFD600);

    public static final Color AMBER = new Color(0xFFC107,
            new int[]{0xFFCA28, 0xFFD54F, 0xFFE082, 0xFFECB3, 0xFFF8E1},
            0xFFB300, 0xFFA000, 0xFF8F00, 0xFF6F00);

    public static final Color AMBER_ACCENT = new Color(0xFFD740, new int[]{0xFFE57F}, 0xFFC400, 0xFFAB00);

    public static final Color ORANGE = new Color(0xFF9800,
            new int[]{0xFFA726, 0xFFB74D, 0xFFCC80, 0xFFE0B2, 0xFFFFF3E0},
            0xFB8C00, 0xF57C00, 0xEF6C00, 0xE65100);

    public static final Color ORANGE_ACCENT = new Color(0xFFAB40, new int[]{0xFFD180}, 0xFF9100, 0xFF6D00);

    public static final Color DEEP_ORANGE = new Color(0xFF5722,
            new int[]{0xFF7043, 0xFF8A65, 0xFFAB91, 0xFFCCBC, 0xFBE9E7},
            0xF4511E, 0xE64A19, 0xD84315, 0xBF360C);

    public static final Color DEEP_ORANGE_ACCENT = new Color(0xFF6E40, new int[]{0xFF9E80}, 0xFF3D00, 0xDD2C00);

    public static final Color BROWN = new Color(0x795548,
            new int[]{0x8D6E63, 0xA1887F, 0xBCAAA4, 0xD7CCC8, 0xEFEBE9},
            0x6D4C41, 0x5D4037, 0x4E342E, 0x3E2723);

    public static final Color GREY = new Color(0x9E9E9E,
            new int[]{0xBDBDBD, 0xE0E0E0, 0xEEEEEE, 0xF5F5F5, 0xFAFAFA},
            0x757575, 0x616161, 0x424242, 0x212121);

    public static final Color BLUE_GREY = new Color(0x607D8B,
            new int[]{0x78909C, 0x90A4AE, 0xB0BEC5, 0xCFD8DC, 0xECEFF1},
            0x546E7A, 0x455A64, 0x37474F, 0x263238);

    public final int normal;
    private final int[] shadeBright;
    private final int[] shadeDark;
    private final int[] all;

    public Color(int normal, int[] shadeBright, int... shadeDark) {
        this.normal = withAlpha(normal, 255);
        this.shadeBright = shadeBright;
        this.shadeDark = shadeDark;
        for (int i = 0; i < this.shadeBright.length; i++) {
            this.shadeBright[i] = withAlpha(this.shadeBright[i], 255);
        }
        for (int i = 0; i < this.shadeDark.length; i++) {
            this.shadeDark[i] = withAlpha(this.shadeDark[i], 255);
        }
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
