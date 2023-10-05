package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.drawable.IInterpolation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.ToIntFunction;

/**
 * Utility class for dealing with colors.
 * <b>All methods assume the color int to be AARRGGBB if not stated otherwise!</b>
 */
public class Color implements IntIterable {

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
     * Converts the HSV format into ARGB. With H being hue, S being saturation and V being value.
     *
     * @param hue        value from 0 to 360 (wraps around)
     * @param saturation value from 0 to 1
     * @param value      value from 0 to 1
     * @param alpha      value from 0 to 1
     * @return the color
     */
    public static int ofHSV(int hue, float saturation, float value, float alpha) {
        hue %= 360;
        if (hue < 0) hue += 360;
        saturation = MathHelper.clamp(saturation, 0f, 1f);
        value = MathHelper.clamp(value, 0f, 1f);
        alpha = MathHelper.clamp(alpha, 0f, 1f);
        float c = value * saturation;
        float x = c * (1 - Math.abs(hue / 60f % 2 - 1));
        float m = value - c;
        return ofHxcm(hue, c, x, m, alpha);
    }

    /**
     * Converts the HSL format into ARGB. With H being hue, S being saturation and L being lightness.
     *
     * @param hue        value from 0 to 360 (wraps around)
     * @param saturation value from 0 to 1
     * @param lightness  value from 0 to 1
     * @param alpha      value from 0 to 1
     * @return the color
     */
    public static int ofHSL(int hue, float saturation, float lightness, float alpha) {
        hue %= 360;
        saturation = MathHelper.clamp(saturation, 0f, 1f);
        lightness = MathHelper.clamp(lightness, 0f, 1f);
        alpha = MathHelper.clamp(alpha, 0f, 1f);
        float c = (1 - Math.abs(2 * lightness - 1)) * saturation;
        float x = c * (1 - Math.abs(hue / 60f % 2 - 1));
        float m = lightness - c / 2;
        return ofHxcm(hue, c, x, m, alpha);
    }

    private static int ofHxcm(int hue, float c, float x, float m, float alpha) {
        if (hue < 60) return argb(c + m, x + m, m, alpha);
        if (hue < 120) return argb(x + m, c + m, m, alpha);
        if (hue < 180) return argb(m, c + m, x + m, alpha);
        if (hue < 240) return argb(m, x + m, c + m, alpha);
        if (hue < 300) return argb(x + m, m, c + m, alpha);
        return argb(c + m, m, x + m, alpha);
    }

    /**
     * Replaces the red bits in the ARGB color.
     *
     * @param argb color in argb format
     * @param red  red value from 0 to 255
     * @return new ARGB color
     */
    public static int withRed(int argb, int red) {
        argb &= ~(0xFF << 16);
        return argb | red << 16;
    }

    /**
     * Replaces the green bits in the ARGB color.
     *
     * @param argb  color in argb format
     * @param green green value from 0 to 255
     * @return new ARGB color
     */
    public static int withGreen(int argb, int green) {
        argb &= ~(0xFF << 8);
        return argb | green << 8;
    }

    /**
     * Replaces the blue bits in the ARGB color.
     *
     * @param argb color in argb format
     * @param blue blue value from 0 to 255
     * @return new ARGB color
     */
    public static int withBlue(int argb, int blue) {
        argb &= ~0xFF;
        return argb | blue;
    }

    /**
     * Replaces the alpha bits in the ARGB color.
     *
     * @param argb  color in argb format
     * @param alpha alpha value from 0 to 255
     * @return new ARGB color
     */
    public static int withAlpha(int argb, int alpha) {
        argb &= ~(0xFF << 24);
        return argb | alpha << 24;
    }

    /**
     * Replaces the red bits in the ARGB color.
     *
     * @param argb color in argb format
     * @param red  red value from 0 to 1
     * @return new ARGB color
     */
    public static int withRed(int argb, float red) {
        return withRed(argb, (int) (red * 255));
    }

    /**
     * Replaces the green bits in the ARGB color.
     *
     * @param argb  color in argb format
     * @param green green value from 0 to 1
     * @return new ARGB color
     */
    public static int withGreen(int argb, float green) {
        return withGreen(argb, (int) (green * 255));
    }

    /**
     * Replaces the blue bits in the ARGB color.
     *
     * @param argb color in argb format
     * @param blue blue value from 0 to 1
     * @return new ARGB color
     */
    public static int withBlue(int argb, float blue) {
        return withBlue(argb, (int) (blue * 255));
    }

    /**
     * Replaces the alpha bits in the ARGB color.
     *
     * @param argb  color in argb format
     * @param alpha alpha value from 0 to 1
     * @return new ARGB color
     */
    public static int withAlpha(int argb, float alpha) {
        return withAlpha(argb, (int) (alpha * 255));
    }

    /**
     * Extracts the red bits from the ARGB color.
     *
     * @return the red value (from 0 to 255)
     */
    public static int getRed(int argb) {
        return argb >> 16 & 255;
    }

    /**
     * Extracts the green bits from the ARGB color.
     *
     * @return the green value (from 0 to 255)
     */
    public static int getGreen(int argb) {
        return argb >> 8 & 255;
    }

    /**
     * Extracts the blue bits from the ARGB color.
     *
     * @return the blue value (from 0 to 255)
     */
    public static int getBlue(int argb) {
        return argb & 255;
    }

    /**
     * Extracts the alpha bits from the ARGB color.
     *
     * @return the alpha value (from 0 to 255)
     */
    public static int getAlpha(int argb) {
        return argb >> 24 & 255;
    }

    /**
     * Extracts the red bits from the ARGB color.
     *
     * @return the red value (from 0 to 1)
     */
    public static float getRedF(int argb) {
        return getRed(argb) / 255f;
    }

    /**
     * Extracts the green bits from the ARGB color.
     *
     * @return the green value (from 0 to 1)
     */
    public static float getGreenF(int argb) {
        return getGreen(argb) / 255f;
    }

    /**
     * Extracts the blue bits from the ARGB color.
     *
     * @return the blue value (from 0 to 1)
     */
    public static float getBlueF(int argb) {
        return getBlue(argb) / 255f;
    }

    /**
     * Extracts the alpha bits from the ARGB color.
     *
     * @return the alpha value (from 0 to 1)
     */
    public static float getAlphaF(int argb) {
        return getAlpha(argb) / 255f;
    }

    /**
     * Calculates the hue value (HSV or HSL format) from the ARGB color.
     *
     * @param argb color
     * @return hue value
     */
    public static int getHue(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        if (r == g && r == b) return 0;
        float min = Math.min(r, Math.min(g, b));
        float result = 0;
        if (r >= g && r >= b) {
            result = ((g - b) / (r - min)) % 6;
        } else if (g >= r && g >= b) {
            result = ((b - r) / (g - min)) + 2;
        } else if (b >= r && b >= g) {
            result = ((r - g) / (b - min)) + 4;
        }
        return (int) (result * 60 + 0.5f);
    }

    /**
     * Calculates the HSV saturation value from the ARGB color.
     *
     * @param argb color
     * @return HSV saturation value.
     */
    public static float getHSVSaturation(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));
        return max == 0 ? 0 : (max - min) / max;
    }

    /**
     * Calculates the HSL saturation value from the ARGB color.
     *
     * @param argb color
     * @return HSL saturation value.
     */
    public static float getHSLSaturation(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));
        return (max - min) / (1 - Math.abs(max + min - 1));
    }

    /**
     * Calculates the HSV value from the ARGB color.
     *
     * @param argb color
     * @return HSV value.
     */
    public static float getValue(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        return Math.max(r, Math.max(g, b));
    }

    /**
     * Calculates the HSL lightness value from the ARGB color.
     *
     * @param argb color
     * @return HSL lightness value.
     */
    public static float getLightness(int argb) {
        float r = getRedF(argb), g = getGreenF(argb), b = getBlueF(argb);
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));
        return (max + min) / 2;
    }

    /**
     * Replaces the hue value in the ARGB color in the HSV format.
     *
     * @param argb color
     * @param hue  new hue
     * @return new ARGB color
     */
    public static int withHSVHue(int argb, int hue) {
        return ofHSV(hue, getHSVSaturation(argb), getValue(argb), getAlphaF(argb));
    }

    /**
     * Replaces the saturation value in the ARGB color in the HSV format.
     *
     * @param argb       color
     * @param saturation new saturation
     * @return new ARGB color
     */
    public static int withHSVSaturation(int argb, float saturation) {
        return ofHSV(getHue(argb), saturation, getValue(argb), getAlphaF(argb));
    }

    /**
     * Replaces the value in the ARGB color in the HSV format.
     *
     * @param argb  color
     * @param value new value
     * @return new ARGB color
     */
    public static int withValue(int argb, float value) {
        return ofHSV(getHue(argb), getHSVSaturation(argb), value, getAlphaF(argb));
    }

    /**
     * Replaces the hue value in the ARGB color in the HSL format.
     *
     * @param argb color
     * @param hue  new hue
     * @return new ARGB color
     */
    public static int withHSLHue(int argb, int hue) {
        return ofHSL(hue, getHSLSaturation(argb), getLightness(argb), getAlphaF(argb));
    }

    /**
     * Replaces the saturation value in the ARGB color in the HSL format.
     *
     * @param argb       color
     * @param saturation new saturation
     * @return new ARGB color
     */
    public static int withHSLSaturation(int argb, float saturation) {
        return ofHSL(getHue(argb), saturation, getLightness(argb), getAlphaF(argb));
    }

    /**
     * Replaces the lightness value in the ARGB color in the HSL format.
     *
     * @param argb      color
     * @param lightness new lightness
     * @return new ARGB color
     */
    public static int withLightness(int argb, float lightness) {
        return ofHSL(getHue(argb), getHSLSaturation(argb), lightness, getAlphaF(argb));
    }

    /**
     * Extracts the RGB bits into an array.
     *
     * @return rgba as an array [red, green, blue]
     */
    public static int[] getRGBValues(int argb) {
        return new int[]{getRed(argb), getGreen(argb), getBlue(argb)};
    }

    /**
     * Extracts the ARGB bits into an array.
     *
     * @return rgba as an array [red, green, blue, alpha]
     */
    public static int[] getARGBValues(int argb) {
        return new int[]{getRed(argb), getGreen(argb), getBlue(argb), getAlpha(argb)};
    }

    /**
     * Converts an RGBA int to an ARGB int.
     *
     * @param rgba RGBA color
     * @return ARGB color
     */
    public static int rgbaToArgb(int rgba) {
        return Color.argb(getAlpha(rgba), getRed(rgba), getGreen(rgba), getBlue(rgba));
    }

    /**
     * Converts an ARGB int to an RGBA int.
     *
     * @param argb ARGB color
     * @return RGBA color
     */
    public static int argbToRgba(int argb) {
        return Color.rgba(getRed(argb), getGreen(argb), getBlue(argb), getAlpha(argb));
    }

    /**
     * Inverts all color bytes except alpha.
     *
     * @param argb ARGB color
     * @return inverted color
     */
    public static int invert(int argb) {
        return Color.argb(255 - getRed(argb), 255 - getGreen(argb), 255 - getBlue(argb), getAlpha(argb));
    }

    /**
     * Multiplies each color byte with a factor to make it darker or brighter.
     *
     * @param argb          ARGB color
     * @param factor        multiplication factor
     * @param multiplyAlpha if alpha byte should be multiplied too
     * @return multiplied ARGB color
     */
    public static int multiply(int argb, float factor, boolean multiplyAlpha) {
        return argb(getRedF(argb) * factor, getGreenF(argb) * factor, getBlueF(argb) * factor, multiplyAlpha ? getAlphaF(argb) * factor : getAlphaF(argb));
    }

    /**
     * Calculates the average of each color byte in the array and puts it into a new ARGB color.
     *
     * @param colors ARGB colors
     * @return average ARGB color
     */
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

    /**
     * Calculates the average of each color byte in the array and puts it into a new ARGB color.
     *
     * @param colorFunction function to extract a ARGB color from the objects
     * @param colorHolders  objects that can be converted to ARGB colors
     * @return average ARGB color
     */
    @SafeVarargs
    public static <T> int average(ToIntFunction<T> colorFunction, T... colorHolders) {
        int r = 0, g = 0, b = 0, a = 0;
        for (T colorHolder : colorHolders) {
            int color = colorFunction.applyAsInt(colorHolder);
            r += getRed(color);
            g += getGreen(color);
            b += getBlue(color);
            a += getAlpha(color);
        }
        return argb(r / colorHolders.length, g / colorHolders.length, b / colorHolders.length, a / colorHolders.length);
    }

    /**
     * Interpolates each color byte between two ARGB colors using linear interpolation and a progress value.
     *
     * @param color1 lower color
     * @param color2 higher color
     * @param value  progress value
     * @return interpolated ARGB color
     */
    public static int interpolate(int color1, int color2, float value) {
        return interpolate(Interpolation.LINEAR, color1, color2, value);
    }

    /**
     * Interpolates each color byte between two ARGB colors with an interpolation curve and a progress value.
     *
     * @param curve  interpolation curve
     * @param color1 lower color
     * @param color2 higher color
     * @param value  progress value
     * @return interpolated ARGB color
     */
    public static int interpolate(IInterpolation curve, int color1, int color2, float value) {
        value = MathHelper.clamp(value, 0, 1);
        int r = (int) curve.interpolate(Color.getRed(color1), Color.getRed(color2), value);
        int g = (int) curve.interpolate(Color.getGreen(color1), Color.getGreen(color2), value);
        int b = (int) curve.interpolate(Color.getBlue(color1), Color.getBlue(color2), value);
        int a = (int) curve.interpolate(Color.getAlpha(color1), Color.getAlpha(color2), value);
        return Color.argb(r, g, b, a);
    }

    /**
     * A helper method to apply a color to rendering.
     * If the alpha is 0 and any other value is not null, the alpha will be set to max.
     *
     * @param color argb color
     */
    @SideOnly(Side.CLIENT)
    public static void setGlColor(int color) {
        if (color == 0) {
            GlStateManager.color(0, 0, 0, 0);
            return;
        }
        float a = getAlphaF(color);
        if (a == 0) a = 1f;
        GlStateManager.color(getRedF(color), getGreenF(color), getBlueF(color), a);
    }

    /**
     * Applies a ARGB color to OpenGL with a fixed alpha value of 255.
     *
     * @param color ARGB color.
     */
    @SideOnly(Side.CLIENT)
    public static void setGlColorOpaque(int color) {
        if (color == 0) {
            GlStateManager.color(0, 0, 0, 0);
            return;
        }
        GlStateManager.color(getRedF(color), getGreenF(color), getBlueF(color), 1f);
    }

    /**
     * Enables the OpenGL color mask and sets its colors to white.
     */
    @SideOnly(Side.CLIENT)
    public static void resetGlColor() {
        GlStateManager.colorMask(true, true, true, true);
        setGlColorOpaque(WHITE.normal);
    }

    /**
     * Parses a ARGB color of a json element.
     *
     * @param jsonElement json element
     * @return ARGB color
     * @throws JsonParseException if color could not be parsed
     */
    public static int ofJson(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            int color = (int) (long) Long.decode(jsonElement.getAsString()); // bruh
            if (color != 0 && getAlpha(color) == 0) {
                return withAlpha(color, 255);
            }
            return color;
        }
        if (jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            String alphaS = JsonHelper.getString(json, "1f", "a", "alpha");
            float alphaF;
            int alpha;
            if (alphaS.contains(".") || alphaS.endsWith("f") || alphaS.endsWith("F") || alphaS.endsWith("d") || alphaS.endsWith("D")) {
                try {
                    alphaF = MathHelper.clamp(Float.parseFloat(alphaS), 0f, 1f);
                    alpha = (int) (alphaF * 255);
                } catch (NumberFormatException e) {
                    throw new JsonParseException("Failed to parse alpha value", e);
                }
            } else {
                try {
                    alpha = MathHelper.clamp(Integer.parseInt(alphaS), 0, 255);
                    alphaF = alpha / 255f;
                } catch (NumberFormatException e) {
                    throw new JsonParseException("Failed to parse alpha value", e);
                }
            }
            if (hasRGB(json)) {
                if (hasHS(json) || hasV(json) || hasL(json))
                    throw new JsonParseException("Found RGB values but also HSV or HSL values!");
                int red = JsonHelper.getInt(json, 255, "r", "red");
                int green = JsonHelper.getInt(json, 255, "g", "green");
                int blue = JsonHelper.getInt(json, 255, "b", "blue");
                if ((red | green | blue) != 0 && alpha == 0) {
                    alpha = 255;
                }
                return Color.argb(red, green, blue, alpha);
            }
            if (hasHS(json)) {
                int hue = JsonHelper.getInt(json, 0, "h", "hue");
                float saturation = JsonHelper.getFloat(json, 0, "s", "saturation");
                if (hasV(json)) {
                    if (hasL(json)) throw new JsonParseException("Found HSV values, but also HSL values!");
                    float value = JsonHelper.getFloat(json, 1f, "v", "value");
                    return ofHSV(hue, saturation, value, alphaF);
                }
                float lightness = JsonHelper.getFloat(json, 0.5f, "l", "lightness");
                return ofHSL(hue, saturation, lightness, alphaF);
            }
            throw new JsonParseException("Empty color declaration");
        }
        throw new JsonParseException("Color must be a primitive or an object!");
    }

    private static boolean hasRGB(JsonObject json) {
        return json.has("r") || json.has("red") ||
                json.has("g") || json.has("green") ||
                json.has("b") || json.has("blue");
    }

    private static boolean hasHS(JsonObject json) {
        return json.has("h") || json.has("hue") ||
                json.has("s") || json.has("saturation");
    }

    private static boolean hasV(JsonObject json) {
        return json.has("v") || json.has("value");
    }

    private static boolean hasL(JsonObject json) {
        return json.has("l") || json.has("lightness");
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
            this.all[index++] = shadeBright[i];
        }
        this.all[index++] = normal;
        for (int shade : shadeDark) {
            this.all[index++] = shade;
        }
    }

    public int bright(int index) {
        return this.shadeBright[index];
    }

    public int dark(int index) {
        return this.shadeDark[index];
    }

    @NotNull
    @Override
    public IntIterator iterator() {
        return IntIterators.wrap(this.all);
    }
}
