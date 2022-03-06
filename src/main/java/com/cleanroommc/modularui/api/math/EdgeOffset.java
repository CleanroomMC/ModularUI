package com.cleanroommc.modularui.api.math;

public class EdgeOffset {

    public final float top, bottom, left, right;

    public EdgeOffset(float left, float top, float right, float bottom) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        if (this.top < 0 || this.bottom < 0 || this.left < 0 || this.right < 0)
            throw new IllegalArgumentException("Margins can't be smaller than zero");
    }

    public static final EdgeOffset ZERO = none();

    public static EdgeOffset all(float all) {
        return new EdgeOffset(all, all, all, all);
    }

    public static EdgeOffset all(float horizontal, float vertical) {
        return new EdgeOffset(horizontal, vertical, horizontal, vertical);
    }

    public static EdgeOffset all(float left, float top, float right, float bottom) {
        return new EdgeOffset(left, top, right, bottom);
    }

    public static EdgeOffset horizontal(float horizontal) {
        return new EdgeOffset(horizontal, 0, horizontal, 0);
    }

    public static EdgeOffset horizontal(float left, float right) {
        return new EdgeOffset(left, 0, right, 0);
    }

    public static EdgeOffset vertical(float vertical) {
        return new EdgeOffset(0, vertical, 0, vertical);
    }

    public static EdgeOffset vertical(float top, float bottom) {
        return new EdgeOffset(0, top, 0, bottom);
    }

    public static EdgeOffset top(float top) {
        return new EdgeOffset(0, top, 0, 0);
    }

    public static EdgeOffset bottom(float bottom) {
        return new EdgeOffset(0, 0, 0, bottom);
    }

    public static EdgeOffset left(float left) {
        return new EdgeOffset(left, 0, 0, 0);
    }

    public static EdgeOffset right(float right) {
        return new EdgeOffset(0, 0, right, 0);
    }

    public static EdgeOffset none() {
        return new EdgeOffset(0, 0, 0, 0);
    }

    public EdgeOffset withTop(float top) {
        return new EdgeOffset(left, top, right, bottom);
    }

    public EdgeOffset withBottom(float bottom) {
        return new EdgeOffset(left, top, right, bottom);
    }

    public EdgeOffset withLeft(float left) {
        return new EdgeOffset(left, top, right, bottom);
    }

    public EdgeOffset withRight(float right) {
        return new EdgeOffset(left, top, right, bottom);
    }

    public boolean isZero() {
        return top == 0 && bottom == 0 && left == 0 && right == 0;
    }
}
