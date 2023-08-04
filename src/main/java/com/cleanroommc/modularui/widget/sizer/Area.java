package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.utils.MathUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Area extends Rectangle implements IResizeable {

    public static final Area SHARED = new Area();

    public int rx, ry;
    private byte panelLayer = 0;
    private int z;
    private final Box margin = new Box();
    private final Box padding = new Box();

    public Area() {
    }

    public Area(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public Area(Rectangle rectangle) {
        super(rectangle);
    }

    public int x() {
        return this.x;
    }

    public void x(int x) {
        this.x = x;
    }

    public int y() {
        return this.y;
    }

    public void y(int y) {
        this.y = y;
    }

    public int w() {
        return this.width;
    }

    public void w(int w) {
        this.width = w;
    }

    public int h() {
        return this.height;
    }

    public void h(int h) {
        this.height = h;
    }

    public int ex() {
        return this.x + this.width;
    }

    public void ex(int ex) {
        this.x = ex - this.width;
    }

    public int ey() {
        return this.y + this.height;
    }

    public void ey(int ey) {
        this.y = ey - this.width;
    }

    public int mx() {
        return (int) (this.x + this.width * 0.5);
    }

    public int my() {
        return (int) (this.y + this.height * 0.5);
    }

    public int z() {
        return this.z;
    }

    public void z(int z) {
        this.z = z;
    }

    public byte getPanelLayer() {
        return this.panelLayer;
    }

    public void setPanelLayer(byte panelLayer) {
        this.panelLayer = panelLayer;
    }

    /**
     * Calculate X based on anchor value
     */
    public int x(float anchor) {
        return this.x + (int) (this.width * anchor);
    }

    /**
     * Calculate X based on anchor value
     */
    public int y(float anchor) {
        return this.y + (int) (this.height * anchor);
    }

    public int getPoint(GuiAxis axis) {
        return axis.isHorizontal() ? this.x : this.y;
    }

    public int getSize(GuiAxis axis) {
        return axis.isHorizontal() ? this.width : this.height;
    }

    public int getRelativePoint(GuiAxis axis) {
        return axis.isHorizontal() ? this.rx : this.ry;
    }

    public void setPoint(GuiAxis axis, int v) {
        if (axis.isHorizontal()) {
            this.x = v;
        } else {
            this.y = v;
        }
    }

    public void setSize(GuiAxis axis, int v) {
        if (axis.isHorizontal()) {
            this.width = v;
        } else {
            this.height = v;
        }
    }

    public void setRelativePoint(GuiAxis axis, int v) {
        if (axis.isHorizontal()) {
            this.rx = v;
        } else {
            this.ry = v;
        }
    }

    void applyPos(int parentX, int parentY) {
        this.x = parentX + this.rx;
        this.y = parentY + this.ry;
    }

    public int requestedWidth() {
        return this.width + this.margin.horizontal();
    }

    public int requestedHeight() {
        return this.height + this.margin.vertical();
    }

    public int relativeEndX() {
        return this.rx + this.width;
    }

    public int relativeEndY() {
        return this.ry + this.height;
    }

    /**
     * Check whether given position is inside the rect.
     * Use {@link com.cleanroommc.modularui.api.widget.IWidget#isInside(IViewportStack, int, int)} rather than this!
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

    // transforms each corner vertex and then puts a min fit rectangle around all vertices
    public void transformAndRectanglerize(IViewportStack stack) {
        int xTL = stack.transformX(this.x, this.y), xTR = stack.transformX(ex(), this.y), xBL = stack.transformX(this.x, ey()), xBR = stack.transformX(ex(), ey());
        int yTL = stack.transformY(this.x, this.y), yTR = stack.transformY(ex(), this.y), yBL = stack.transformY(this.x, ey()), yBR = stack.transformY(ex(), ey());
        int x0 = MathUtils.min(xTL, xTR, xBL, xBR);
        int x1 = MathUtils.max(xTL, xTR, xBL, xBR);
        int y0 = MathUtils.min(yTL, yTR, yBL, yBR);
        int y1 = MathUtils.max(yTL, yTR, yBL, yBR);
        setPos(x0, y0, x1, y1);
    }

    public Box getMargin() {
        return this.margin;
    }

    public Box getPadding() {
        return this.padding;
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
