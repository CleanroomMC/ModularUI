package com.cleanroommc.modularui.api.math;

public class Color {

    /**
     * Creates a color int. All values should be 0 - 255
     */
    public static int rgb(int red, int green, int blue) {
        return rgba(red, green, blue, 255);
    }

    /**
     * Creates a color int. All values should be 0 - 255
     */
    public static int rgba(int red, int green, int blue, int alpha) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | ((blue & 0xFF));
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
        return rgba(red, green, blue, 1f);
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
        return rgba(r + m, g + m, b + m, alpha);
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
     * @return rgba as an array [red, green, blue, alpha]
     */
    public static int[] getValues(int rgba) {
        return new int[]{getRed(rgba), getGreen(rgba), getBlue(rgba), getAlpha(rgba)};
    }

    private Color() {
    }
}
