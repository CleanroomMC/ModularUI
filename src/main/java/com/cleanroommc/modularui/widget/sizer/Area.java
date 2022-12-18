package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.api.IGuiElement;
import com.cleanroommc.modularui.utils.MathUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Area extends Rectangle implements IResizeable {

    public static final Area SHARED = new Area();

    private int z;

    public Area() {
    }

    public Area(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public Area(Rectangle rectangle) {
        super(rectangle);
    }

    public int x() {
        return x;
    }

    public void x(int x) {
        this.x = x;
    }

    public int y() {
        return y;
    }

    public void y(int y) {
        this.y = y;
    }

    public int w() {
        return width;
    }

    public void w(int w) {
        this.width = w;
    }

    public int h() {
        return height;
    }

    public void h(int h) {
        this.height = h;
    }

    public int ex() {
        return x + width;
    }

    public void ex(int ex) {
        this.x = ex - width;
    }

    public int ey() {
        return y + height;
    }

    public void ey(int ey) {
        this.y = ey - width;
    }

    public int mx() {
        return (int) (x + width * 0.5);
    }

    public int my() {
        return (int) (y + height * 0.5);
    }

    public int z() {
        return z;
    }

    public void z(int z) {
        this.z = z;
    }

    /**
     * Check whether given position is inside of the rect
     */
    public boolean isInside(int x, int y) {
        return x >= this.x && x < this.x + this.width && y >= this.y && y < this.y + this.height;
    }

    /**
     * Check whether given rect intersects this rect
     */
    public boolean intersects(Rectangle2D area) {
        return this.x < area.getX() + area.getWidth() && this.y < area.getY() + area.getHeight()
                && area.getX() < this.x + this.width && area.getY() < this.y + this.height;
    }

    /**
     * Clamp given area inside of this one
     */
    public void clamp(Area area) {
        int x1 = area.x();
        int y1 = area.y();
        int x2 = area.ex();
        int y2 = area.ey();

        x1 = MathUtils.clamp(x1, this.x, this.ex());
        y1 = MathUtils.clamp(y1, this.y, this.ey());
        x2 = MathUtils.clamp(x2, this.x, this.ex());
        y2 = MathUtils.clamp(y2, this.y, this.ey());

        area.setPos(x1, y1, x2, y2);
    }

    /**
     * Expand the area either inwards or outwards on each side
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
        this.width += grow;
    }

    public void growH(int grow) {
        this.height += grow;
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
        this.width = w;
        this.height = h;
    }

    public void setPos(int sx, int sy, int ex, int ey) {
        int x0 = Math.min(sx, ex);
        int y0 = Math.min(sy, ey);
        ex = Math.max(sx, ex);
        ey = Math.max(sy, ey);
        setPos(x0, y0);
        setSize(ex - x0, ey - y0);
    }

    public void reset() {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
    }

    public void set(Rectangle area) {
        setBounds(area.x, area.y, area.width, area.height);
    }

    @Override
    public void apply(IGuiElement guiElement) {
        guiElement.getArea().set(this);
    }

    @Override
    public void postApply(IGuiElement guiElement) {
    }

    public Area createCopy() {
        return new Area(this);
    }
}
