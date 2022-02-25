package com.cleanroommc.modularui.api.math;

import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Objects;

public class Pos2d {

    public static final Pos2d ZERO = zero();

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

    public static Pos2d ofJson(JsonElement jsonElement) {
        float x = 0, y = 0;
        if (jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            x = JsonHelper.getFloat(json, 0, "x");
            y = JsonHelper.getFloat(json, 0, "y");
        } else {
            String raw = jsonElement.getAsString();
            if (raw.contains(",")) {
                String[] parts = raw.split(",");
                try {
                    if (!parts[0].isEmpty()) {
                        x = Float.parseFloat(parts[0]);
                    }
                    if(parts.length > 1 && !parts[1].isEmpty()) {
                        y = Float.parseFloat(parts[1]);
                    }
                } catch (NumberFormatException e) {
                    ModularUIMod.LOGGER.error("Error parsing JSON pos: {}", raw);
                    e.printStackTrace();
                }
            }
        }
        return new Pos2d(x, y);
    }
}
