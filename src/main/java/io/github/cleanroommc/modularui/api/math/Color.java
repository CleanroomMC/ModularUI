package io.github.cleanroommc.modularui.api.math;

import java.util.Objects;

public class Color {

    private final int rgba;

    public Color(int color) {
        this.rgba = color;
    }

    /**
     * all params should be between 0 - 255
     *
     * @return color
     * @throws IllegalArgumentException if a value is not between 0 - 255
     */
    public static Color of(int red, int green, int blue, int alpha) {
        if (red > 255 || red < 0)
            throw new IllegalArgumentException("Red is not within bounds. Should be 0 - 255, but is " + red);
        if (green > 255 || green < 0)
            throw new IllegalArgumentException("Green is not within bounds. Should be 0 - 255, but is " + red);
        if (blue > 255 || blue < 0)
            throw new IllegalArgumentException("Blue is not within bounds. Should be 0 - 255, but is " + red);
        if (alpha > 255 || alpha < 0)
            throw new IllegalArgumentException("Alpha is not within bounds. Should be 0 - 255, but is " + red);
        return new Color(((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | ((blue & 0xFF)));
    }

    /**
     * all params should be between 0 - 255
     *
     * @return color with full opacity
     * @throws IllegalArgumentException if a value is not between 0 - 255
     */
    public static Color of(int red, int green, int blue) {
        return of(red, green, blue, 255);
    }

    /**
     * all params should be between 0 - 1
     *
     * @return color
     * @throws IllegalArgumentException if a value is not between 0 - 1
     */
    public static Color of(float red, float green, float blue, float alpha) {
        return Color.of((int) (red * 255 + .5f), (int) (green * 255 + .5f), (int) (blue * 255 + .5f), (int) (alpha * 255 + .5f));
    }

    /**
     * all params should be between 0 - 1
     *
     * @return color with full opacity
     * @throws IllegalArgumentException if a value is not between 0 - 1
     */
    public static Color of(float red, float green, float blue) {
        return of(red, green, blue, 1f);
    }

    /**
     * Converts a HSV color to rgba
     *
     * @param hue        value from 0 to 360
     * @param saturation value from 0 to 1 (will be divided by 100 if it is larger than 1)
     * @param value      value from 0 to 1 (will be divided by 100 if it is larger than 1)
     * @param alpha      value from 0 to 1
     * @return the color
     * @throws IllegalArgumentException if hue is not within bounds
     */
    public static Color ofHSV(int hue, float saturation, float value, float alpha) {
        if (hue > 360 || hue < 0) throw new IllegalArgumentException("Hue is not within bounds: " + hue);
        if (saturation > 1) saturation = saturation / 100;
        if (value > 1) value = value / 100;

        double c = value * saturation;
        double x = c * (1 - (((hue / 60f) % 2) - 1));
        x = Math.max(x, -x);
        double m = value - c;
        double r, g, b;
        if (hue < 60) {
            r = c; g = x; b = 0;
        } else if (hue < 120) {
            r = x; g = c; b = 0;
        } else if (hue < 180) {
            r = 0; g = c; b = x;
        } else if (hue < 240) {
            r = 0; g = x; b = c;
        } else if (hue < 300) {
            r = x; g = 0; b = c;
        } else {
            r = c; g = 0; b = x;
        }
        int red = (int) ((r + m) * 255 + 0.5);
        int green = (int) ((g + m) * 255 + 0.5);
        int blue = (int) ((b + m) * 255 + 0.5);
        return Color.of(red, green, blue, (int) (alpha * 255 + 0.5));
    }

    /**
     * @param r red value (0 - 255)
     * @return the same color but with the given red value
     */
    public Color withRed(int r) {
        int g = getGreen(), b = getBlue(), a = getAlpha();
        return Color.of(r, g, b, a);
    }

    /**
     * @param b blue value (0 - 255)
     * @return the same color but with the given blue value
     */
    public Color withBlue(int b) {
        int g = getGreen(), r = getRed(), a = getAlpha();
        return Color.of(r, g, b, a);
    }

    /**
     * @param g green value (0 - 255)
     * @return the same color but with the given green value
     */
    public Color withGreen(int g) {
        int r = getRed(), b = getBlue(), a = getAlpha();
        return Color.of(r, g, b, a);
    }

    /**
     * @param a alpha value (0 - 255)
     * @return the same color but with the given alpha value
     */
    public Color withAlpha(int a) {
        int g = getGreen(), b = getBlue(), r = getRed();
        return Color.of(r, g, b, a);
    }

    /**
     * @param opacity opacity value (0 - 1)
     * @return the same color but with the given opacity value
     */
    public Color withOpacity(double opacity) {
        return withAlpha((int) (opacity * 255 + 0.5));
    }

    /**
     * @return the red value
     */
    public int getRed() {
        return rgba >> 16 & 255;
    }

    /**
     * @return the green value
     */
    public int getGreen() {
        return rgba >> 8 & 255;
    }

    /**
     * @return the blue value
     */
    public int getBlue() {
        return rgba & 255;
    }

    /**
     * @return the alpha value
     */
    public int getAlpha() {
        return rgba >> 24 & 255;
    }

    /**
     * @return rgba as an array [red, green, blue, alpha]
     */
    public int[] getValues() {
        return new int[]{getRed(), getGreen(), getBlue(), getAlpha()};
    }

    /**
     * @return the raw rgba
     */
    public int asInt() {
        return rgba;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Color color = (Color) o;
        return rgba == color.rgba;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rgba);
    }

    @Override
    public String toString() {
        return "Color{" +
                "r=" + getRed() +
                ", g=" + getGreen() +
                ", b=" + getBlue() +
                ", a=" + getAlpha() +
                '}';
    }
}
