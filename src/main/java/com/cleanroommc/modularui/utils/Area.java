package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.IResizer;
import com.cleanroommc.modularui.screen.GuiContext;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

/**
 * Utility class for boxes
 * <p>
 * Used in GUI for rendering and locating cursor inside of the box purposes.
 */
public class Area implements IResizer {

    /**
     * Shared area which could be used for calculations without creating new
     * instances
     */
    public static final Area SHARED = new Area();

    /**
     * X position coordinate of the box
     */
    public int x;

    /**
     * Y position coordinate of the box
     */
    public int y;

    /**
     * Width of the box
     */
    public int w;

    /**
     * Height of the box
     */
    public int h;

    public Area() {
    }

    public Area(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public int getIndex(int x, int y, int size) {
        return MathUtils.gridIndex(x - this.x, y - this.y, size, this.w);
    }

    public int getRows(int count, int size) {
        return MathUtils.gridRows(count, size, this.w);
    }

    @SideOnly(Side.CLIENT)
    public boolean isInside(GuiContext context) {
        return this.isInside(context.mouseX, context.mouseY);
    }

    /**
     * Check whether given position is inside of the rect
     */
    public boolean isInside(int x, int y) {
        return x >= this.x && x < this.x + this.w && y >= this.y && y < this.y + this.h;
    }

    /**
     * Check whether given rect intersects this rect
     */
    public boolean intersects(Area area) {
        return this.x < area.x + area.w && this.y < area.y + area.h
                && area.x < this.x + this.w && area.y < this.y + this.h;
    }

    /**
     * Clamp given area inside of this one
     */
    public void clamp(Area area) {
        int x1 = area.x;
        int y1 = area.y;
        int x2 = area.ex();
        int y2 = area.ey();

        x1 = MathUtils.clamp(x1, this.x, this.ex());
        y1 = MathUtils.clamp(y1, this.y, this.ey());
        x2 = MathUtils.clamp(x2, this.x, this.ex());
        y2 = MathUtils.clamp(y2, this.y, this.ey());

        area.setPoints(x1, y1, x2, y2);
    }

    /**
     * Expand the area either inwards or outwards
     */
    public void expand(int offset) {
        this.expandX(offset);
        this.expandY(offset);
    }

    /**
     * Expand the area either inwards or outwards (horizontally)
     */
    public void expandX(int offset) {
        offsetX(-offset);
        growW(offset * 2);
    }

    /**
     * Expand the area either inwards or outwards (horizontally)
     */
    public void expandY(int offset) {
        offsetY(-offset);
        growH(offset * 2);
    }

    public void offset(int offset) {
        offsetX(offset);
        offsetY(offset);
    }

    public void offset(int offsetX, int offsetY) {
        offsetX(offsetX);
        offsetY(offsetY);
    }

    public void offsetX(int offset) {
        this.x += offset;
    }

    public void offsetY(int offset) {
        this.y += offset;
    }

    public void grow(int grow) {
        growW(grow);
        growH(grow);
    }

    public void grow(int growW, int growH) {
        growW(growW);
        growH(growH);
    }

    public void growW(int grow) {
        this.w += grow;
    }

    public void growH(int grow) {
        this.h += grow;
    }

    /**
     * Set all values
     */
    public void set(int x, int y, int w, int h) {
        this.setPos(x, y);
        this.setSize(w, h);
    }

    /**
     * Set the position
     */
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Set the size
     */
    public void setSize(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public void setPoints(int x1, int y1, int x2, int y2) {
        this.setPoints(x1, y1, x2, y2, 0);
    }

    public void setPoints(int x1, int y1, int x2, int y2, int offset) {
        int mx = Math.max(x1, x2);
        int my = Math.max(y1, y2);
        int nx = Math.min(x1, x2);
        int ny = Math.min(y1, y2);

        this.x = nx - offset;
        this.y = ny - offset;
        this.w = mx - nx + offset;
        this.h = my - ny + offset;
    }

    /**
     * Copy properties from other area
     */
    public void copy(Area area) {
        this.x = area.x;
        this.y = area.y;
        this.w = area.w;
        this.h = area.h;
    }

    /**
     * Calculate X based on anchor value
     */
    public int x(float anchor) {
        return this.x + (int) (this.w * anchor);
    }

    /**
     * Calculate X based on anchor value with additional value
     */
    public int x(float anchor, int value) {
        return this.x + (int) ((this.w - value) * anchor);
    }

    /**
     * Calculate mid point X value
     */
    public int mx() {
        return this.x + (int) (this.w * 0.5F);
    }

    /**
     * Calculate mid point X value
     */
    public int mx(int value) {
        return this.x + (int) ((this.w - value) * 0.5F);
    }

    /**
     * Calculate end point X (right) value
     */
    public int ex() {
        return this.x + this.w;
    }

    /**
     * Calculate Y based on anchor value
     */
    public int y(float anchor) {
        return this.y + (int) (this.h * anchor);
    }

    /**
     * Calculate Y based on anchor value
     */
    public int y(float anchor, int value) {
        return this.y + (int) ((this.h - value) * anchor);
    }

    /**
     * Calculate mid point Y value
     */
    public int my() {
        return this.y + (int) (this.h * 0.5F);
    }

    /**
     * Calculate mid point Y value
     */
    public int my(int value) {
        return this.y + (int) ((this.h - value) * 0.5F);
    }

    /**
     * Calculate end point Y (bottom) value
     */
    public int ey() {
        return this.y + this.h;
    }

    /**
     * Draw a rect within the bound of this rect
     */
    @SideOnly(Side.CLIENT)
    public void draw(int color) {
        this.draw(color, 0, 0, 0, 0);
    }

    /**
     * Draw a rect within the bound of this rect
     */
    @SideOnly(Side.CLIENT)
    public void draw(int color, int offset) {
        this.draw(color, offset, offset, offset, offset);
    }

    /**
     * Draw a rect within the bound of this rect
     */
    @SideOnly(Side.CLIENT)
    public void draw(int color, int horizontal, int vertical) {
        this.draw(color, horizontal, vertical, horizontal, vertical);
    }

    /**
     * Draw a rect within the bound of this rect
     */
    @SideOnly(Side.CLIENT)
    public void draw(int color, int lx, int ty, int rx, int by) {
        Gui.drawRect(this.x + lx, this.y + ty, this.ex() - rx, this.ey() - by, color);
    }

    /* IResizer implementation */

    @Override
    public void preApply(Area area) {
    }

    @Override
    public void apply(Area area) {
        area.copy(this);
    }

    @Override
    public void postApply(Area area) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void add(IWidget parent, IWidget child) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void remove(IWidget parent, IWidget child) {
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getW() {
        return this.w;
    }

    @Override
    public int getH() {
        return this.h;
    }

    public ImMut toImmutable() {
        return new ImMut(x, y, w, h);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Area) {
            Area area = (Area) o;
            return x == area.x && y == area.y && w == area.w && h == area.h;
        }
        if (o instanceof ImMut) {
            ImMut area = (ImMut) o;
            return x == area.x && y == area.y && w == area.w && h == area.h;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, w, h);
    }

    @Override
    public String toString() {
        return "Area{" +
                "x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                '}';
    }

    public static class ImMut {

        public static final ImMut ZERO = new ImMut(0, 0, 0, 0);

        public final int x, y, w, h;

        public ImMut(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public Area toArea() {
            return new Area(x, y, w, h);
        }

        /**
         * Calculate X based on anchor value
         */
        public int x(float anchor) {
            return this.x + (int) (this.w * anchor);
        }

        /**
         * Calculate X based on anchor value with additional value
         */
        public int x(float anchor, int value) {
            return this.x + (int) ((this.w - value) * anchor);
        }

        /**
         * Calculate mid point X value
         */
        public int mx() {
            return this.x + (int) (this.w * 0.5F);
        }

        /**
         * Calculate mid point X value
         */
        public int mx(int value) {
            return this.x + (int) ((this.w - value) * 0.5F);
        }

        /**
         * Calculate end point X (right) value
         */
        public int ex() {
            return this.x + this.w;
        }

        /**
         * Calculate Y based on anchor value
         */
        public int y(float anchor) {
            return this.y + (int) (this.h * anchor);
        }

        /**
         * Calculate Y based on anchor value
         */
        public int y(float anchor, int value) {
            return this.y + (int) ((this.h - value) * anchor);
        }

        /**
         * Calculate mid point Y value
         */
        public int my() {
            return this.y + (int) (this.h * 0.5F);
        }

        /**
         * Calculate mid point Y value
         */
        public int my(int value) {
            return this.y + (int) ((this.h - value) * 0.5F);
        }

        /**
         * Calculate end point Y (bottom) value
         */
        public int ey() {
            return this.y + this.h;
        }

        /**
         * Check whether given position is inside of the rect
         */
        public boolean isInside(int x, int y) {
            return x >= this.x && x < this.x + this.w && y >= this.y && y < this.y + this.h;
        }

        /**
         * Check whether given rect intersects this rect
         */
        public boolean intersects(Area area) {
            return this.x < area.x + area.w && this.y < area.y + area.h
                    && area.x < this.x + this.w && area.y < this.y + this.h;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (o instanceof Area) {
                Area area = (Area) o;
                return x == area.x && y == area.y && w == area.w && h == area.h;
            }
            if (o instanceof ImMut) {
                ImMut area = (ImMut) o;
                return x == area.x && y == area.y && w == area.w && h == area.h;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, w, h);
        }

        @Override
        public String toString() {
            return "Area{" +
                    "x=" + x +
                    ", y=" + y +
                    ", w=" + w +
                    ", h=" + h +
                    '}';
        }
    }
}