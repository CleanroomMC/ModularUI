package com.cleanroommc.modularui.utils;

import org.mariuszgromada.math.mxparser.Constant;
import org.mariuszgromada.math.mxparser.Expression;

public class MathUtils {

    // SI prefixes
    public static final Constant k = new Constant("k", 1e3);
    public static final Constant M = new Constant("M", 1e6);
    public static final Constant G = new Constant("G", 1e9);
    public static final Constant T = new Constant("T", 1e12);
    public static final Constant P = new Constant("P", 1e15);
    public static final Constant E = new Constant("E", 1e18);
    public static final Constant Z = new Constant("Z", 1e21);
    public static final Constant Y = new Constant("Y", 1e24);
    public static final Constant m = new Constant("m", 1e-3);
    public static final Constant u = new Constant("u", 1e-6);
    public static final Constant n = new Constant("n", 1e-9);
    public static final Constant p = new Constant("p", 1e-12);
    public static final Constant f = new Constant("f", 1e-15);
    public static final Constant a = new Constant("a", 1e-18);
    public static final Constant z = new Constant("z", 1e-21);
    public static final Constant y = new Constant("y", 1e-24);

    public static ParseResult parseExpression(String expression) {
        return parseExpression(expression, Double.NaN, false);
    }

    public static ParseResult parseExpression(String expression, boolean useSiPrefixes) {
        return parseExpression(expression, Double.NaN, useSiPrefixes);
    }

    public static ParseResult parseExpression(String expression, double defaultValue) {
        return parseExpression(expression, defaultValue, true);
    }

    public static ParseResult parseExpression(String expression, double defaultValue, boolean useSiPrefixes) {
        if (expression == null || expression.isEmpty()) return ParseResult.success(defaultValue);
        Expression e = new Expression(expression);
        if (useSiPrefixes) {
            e.addConstants(k, M, G, T, P, E, Z, Y, m, u, n, p, f, a, z, y);
        }
        double result = e.calculate();
        if (Double.isNaN(result)) {
            return ParseResult.failure(defaultValue, e.getErrorMessage());
        }
        return ParseResult.success(result);
    }

    public static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(v, max));
    }

    public static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(v, max));
    }

    public static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(v, max));
    }

    public static long clamp(long v, long min, long max) {
        return Math.max(min, Math.min(v, max));
    }

    public static int cycler(int x, int min, int max) {
        return x < min ? max : (x > max ? min : x);
    }

    public static float cycler(float x, float min, float max) {
        return x < min ? max : (x > max ? min : x);
    }

    public static double cycler(double x, double min, double max) {
        return x < min ? max : (x > max ? min : x);
    }

    public static int gridIndex(int x, int y, int size, int width) {
        x = x / size;
        y = y / size;

        return x + y * width / size;
    }

    public static int gridRows(int count, int size, int width) {
        double x = count * size / (double) width;

        return count <= 0 ? 1 : (int) Math.ceil(x);
    }

    public static int min(int... values) {
        if (values == null || values.length == 0) throw new IllegalArgumentException();
        if (values.length == 1) return values[0];
        if (values.length == 2) return Math.min(values[0], values[1]);
        int min = Integer.MAX_VALUE;
        for (int i : values) {
            if (i < min) {
                min = i;
            }
        }
        return min;
    }

    public static int max(int... values) {
        if (values == null || values.length == 0) throw new IllegalArgumentException();
        if (values.length == 1) return values[0];
        if (values.length == 2) return Math.max(values[0], values[1]);
        int max = Integer.MIN_VALUE;
        for (int i : values) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }
}