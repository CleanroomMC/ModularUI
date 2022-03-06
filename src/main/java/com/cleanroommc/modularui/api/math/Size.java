package com.cleanroommc.modularui.api.math;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;
import java.util.Objects;

public class Size {

    public static final Size ZERO = zero();

    public static Size zero() {
        return new Size(0, 0);
    }

    public final int width, height;

    public Size(int width, int height) {
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
    }

    public static Size ofDimension(Dimension dimension) {
        return new Size(dimension.width, dimension.height);
    }

    public boolean isLargerThan(Size size) {
        return (size.width * size.height) < (width * height);
    }

    public boolean hasLargerDimensionsThan(Size size) {
        return width > size.width && height > size.height;
    }

    /**
     * @param size to center
     * @return the point of the top left corner
     */
    public Pos2d getCenteringPointForChild(Size size) {
        return new Pos2d((width - size.width) / 2, (height - size.height) / 2);
    }

    public boolean isZero() {
        return width == 0 && height == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Size size = (Size) o;
        return Float.compare(size.width, width) == 0 && Float.compare(size.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return "[" + width + ", " + height + "]";
    }

    public Dimension asDimension() {
        return new Dimension(width, height);
    }

    public static Size ofJson(JsonElement jsonElement) {
        int width = 0, height = 0;
        if (jsonElement.isJsonObject()) {
            JsonObject json = jsonElement.getAsJsonObject();
            width = JsonHelper.getInt(json, 0, "width", "w");
            height = JsonHelper.getInt(json, 0, "height", "h");
        } else {
            String raw = jsonElement.getAsString();
            if (raw.contains(",")) {
                String[] parts = raw.split(",");
                try {
                    if (!parts[0].isEmpty()) {
                        width = Integer.parseInt(parts[0]);
                    }
                    if(parts.length > 1 && !parts[1].isEmpty()) {
                        height = Integer.parseInt(parts[1]);
                    }
                } catch (NumberFormatException e) {
                    ModularUI.LOGGER.error("Error parsing JSON pos: {}", raw);
                    e.printStackTrace();
                }
            }
        }
        return new Size(width, height);
    }
}
