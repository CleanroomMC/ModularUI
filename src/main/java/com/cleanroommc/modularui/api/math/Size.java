package com.cleanroommc.modularui.api.math;

import java.util.Objects;

public class Size {

    public static Size zero() {
        return new Size(0, 0);
    }

    public final float width, height;

    public Size(float width, float height) {
        if(width < 0) throw new IllegalArgumentException("Width in size can't be smaller than 0");
        if(height < 0) throw new IllegalArgumentException("Height in size can't be smaller than 0");
        this.width = width;
        this.height = height;
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
}
