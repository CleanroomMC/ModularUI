package com.cleanroommc.modularui.api.math;

import java.awt.*;
import java.util.Objects;

/**
 * Describes an area in a gui
 * Immutable!
 */
public final class GuiArea {

    public final int x0, x1, y0, y1;
    public final int width, height;

    public GuiArea(int x0, int x1, int y0, int y1) {
        this.x0 = Math.min(x0, x1);
        this.x1 = Math.max(x0, x1);
        this.y0 = Math.min(y0, y1);
        this.y1 = Math.max(y0, y1);
        this.width = this.x1 - this.x0;
        this.height = this.y1 - this.y0;
    }

    public static GuiArea ofRectangle(Rectangle rectangle) {
        return GuiArea.ltwh(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public static GuiArea ofPoints(Pos2d p0, Pos2d p1) {
        return new GuiArea(p0.x, p1.x, p0.y, p1.y);
    }

    public static GuiArea of(Size size, Pos2d pos) {
        return GuiArea.ltwh(pos.x, pos.y, size.width, size.height);
    }

    /**
     * Makes an area of the top left corner and width and height
     * left - top - width - height
     */
    public static GuiArea ltwh(int x, int y, int width, int height) {
        return new GuiArea(x, x + width, y, y + height);
    }

    public GuiArea withSize(int width, int height) {
        return new GuiArea(x0, x0 + width, y0, y0 + height);
    }

    public GuiArea withSize(Size size) {
        return withSize(size.width, height);
    }

    public GuiArea withPos(int x, int y) {
        return new GuiArea(x, x + width, y, y + height);
    }

    public GuiArea translate(int x, int y) {
        return new GuiArea(x0 + x, x1 + x, y0 + y, y1 + y);
    }

    public GuiArea scale(float scale, float xPivot, float yPivot) {
        return scale(scale, scale, xPivot, yPivot);
    }

    public GuiArea scale(float xScale, float yScale, float xPivot, float yPivot) {
        int x0 = this.x0, x1 = this.x1, y0 = this.y0, y1 = this.y1;
        x0 = (int) (xPivot - (xPivot - x0) * xScale);
        y0 = (int) (yPivot - (yPivot - y0) * yScale);
        x1 = (int) (xPivot + (x1 - xPivot) * xScale);
        y1 = (int) (yPivot + (y1 - yPivot) * yScale);
        return new GuiArea(x0, x1, y0, y1);
    }

    public boolean contains(float x, float y) {
        return x >= x0 && x <= x1 && y >= y0 && y <= y1;
    }

    public boolean contains(Pos2d pos) {
        return contains(pos.getX(), pos.getY());
    }

    /**
     * @param bounds to check
     * @return if bounds are partly inside this bounds
     */
    public boolean intersects(GuiArea bounds) {
        for (Alignment alignment : Alignment.CORNERS) {
            if (contains(bounds.corner(alignment))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param bounds to check
     * @return if bounds are fully inside this bounds
     */
    public boolean covers(GuiArea bounds) {
        for (Alignment alignment : Alignment.CORNERS) {
            if (!contains(bounds.corner(alignment))) {
                return false;
            }
        }
        return true;
    }

    public Pos2d getCenter() {
        return new Pos2d((x1 - x0) / 2 + x0, (y1 - y0) / 2 + y0);
    }

    public Pos2d getTopLeft() {
        return new Pos2d(x0, y0);
    }

    public Pos2d corner(Alignment alignment) {
        Pos2d center = getCenter();
        int x = (int) (center.getX() + width / 2 * alignment.x);
        int y = (int) (center.getY() + height / 2 * alignment.y);
        return new Pos2d(x, y);
    }

    public float getArea() {
        return width * height;
    }

    public Size getSize() {
        return new Size(width, height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuiArea GuiArea = (GuiArea) o;
        return GuiArea.x0 == x0 && GuiArea.x1 == x1 && GuiArea.y0 == y0 && GuiArea.y1 == y1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x0, x1, y0, y1);
    }

    @Override
    public String toString() {
        return "Size:[" + width + ", " + height + "], Pos:[" + x0 + ", " + y0 + "]";
    }

    public Rectangle asRectangle() {
        return new Rectangle(x0, y0, width, height);
    }
}
