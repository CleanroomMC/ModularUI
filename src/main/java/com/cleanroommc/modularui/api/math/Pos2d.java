package com.cleanroommc.modularui.api.math;

import java.util.Objects;

public class Pos2d {

    public static Pos2d zero() {
        return new Pos2d(0, 0);
    }

    public final float x, y;

    public Pos2d(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Pos2d(double x, double y) {
        this((float) x, (float) y);
    }

    public static Pos2d cartesian(float x, float y) {
        return new Pos2d(x, y);
    }

    public static Pos2d polar(float angle, float length) {
        float sin = (float) Math.sin(Math.toRadians(angle));
        float cos = (float) Math.cos(Math.toRadians(angle));
        return new Pos2d(cos * length, sin * length);
    }

    public Pos2d add(Pos2d p) {
        return new Pos2d(x + p.x, y + p.y);
    }

    public Pos2d add(float x, float y) {
        return new Pos2d(x + this.x, y + this.y);
    }

    public Pos2d subtract(Pos2d p) {
        return new Pos2d(x - p.x, y - p.y);
    }

    public double distance(Pos2d p) {
        float x = Math.max(this.x - p.x, p.x - this.x);
        float y = Math.max(this.y - p.y, p.y - this.y);
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public float angle(Pos2d p) {
        float x = this.x - p.x;
        float y = this.y - p.y;
        return (float) Math.toDegrees(Math.atan(y / x)) + 90;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isZero() {
        return x == 0 && y == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pos2d pos = (Pos2d) o;
        return Float.compare(pos.x, x) == 0 && Float.compare(pos.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ']';
    }

}
