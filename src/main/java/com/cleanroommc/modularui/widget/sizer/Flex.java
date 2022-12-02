package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.GuiErrorHandler;
import com.cleanroommc.modularui.api.IGuiElement;
import com.cleanroommc.modularui.utils.Alignment;

public class Flex implements IResizeable {

    private final Unit x = new Unit();
    private final Unit y = new Unit();
    private final Unit x2 = new Unit();
    private final Unit y2 = new Unit();
    private Unit left, right, top, bottom, width, height;
    private final IGuiElement parent;
    private Area relativeTo;
    private boolean relativeToParent = true;
    private boolean defaultMode = false;

    public Flex(IGuiElement parent) {
        this.parent = parent;
    }

    public Flex startDefaultMode() {
        this.defaultMode = true;
        return this;
    }

    public Flex endDefaultMode() {
        this.defaultMode = false;
        return this;
    }

    public Flex relative(IGuiElement guiElement) {
        return relative(guiElement.getArea());
    }

    public Flex relative(Area guiElement) {
        this.relativeTo = guiElement;
        this.relativeToParent = false;
        return this;
    }

    public Flex relativeToScreen() {
        this.relativeTo = null;
        this.relativeToParent = false;
        return this;
    }

    public Flex relativeToParent() {
        this.relativeToParent = true;
        return this;
    }

    public Flex left(int x) {
        return left(x, 0, 0, Unit.Measure.PIXEL, true);
    }

    public Flex left(float x) {
        return left(x, 0, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex left(float x, int offset) {
        return left(x, offset, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex left(float x, float anchor) {
        return left(x, 0, anchor, Unit.Measure.RELATIVE, false);
    }

    public Flex left(float x, int offset, float anchor, Unit.Measure measure) {
        return left(x, offset, anchor, measure, false);
    }

    private Flex left(float x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getLeft(), x, offset, anchor, measure, autoAnchor);
    }

    public Flex right(int x) {
        return right(x, 0, 0, Unit.Measure.PIXEL, true);
    }

    public Flex right(float x) {
        return right(x, 0, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex right(float x, int offset) {
        return right(x, offset, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex right(float x, float anchor) {
        return right(x, 0, anchor, Unit.Measure.RELATIVE, false);
    }

    public Flex right(float x, int offset, float anchor, Unit.Measure measure) {
        return right(x, offset, anchor, measure, false);
    }

    private Flex right(float x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getRight(), x, offset, anchor, measure, autoAnchor);
    }

    private Flex unit(Unit u, float val, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        u.setValue(val);
        u.setMeasure(measure);
        u.setOffset(offset);
        u.setAnchor(anchor);
        u.setAutoAnchor(autoAnchor);
        return this;
    }

    public Flex top(int y) {
        return top(y, 0, 0, Unit.Measure.PIXEL, true);
    }

    public Flex top(float y) {
        return top(y, 0, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex top(float y, int offset) {
        return top(y, offset, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex top(float y, float anchor) {
        return top(y, 0, anchor, Unit.Measure.RELATIVE, false);
    }

    public Flex top(float y, int offset, float anchor, Unit.Measure measure) {
        return top(y, offset, anchor, measure, false);
    }

    private Flex top(float y, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getTop(), y, offset, anchor, measure, autoAnchor);
    }

    public Flex bottom(int y) {
        return bottom(y, 0, 0, Unit.Measure.PIXEL, true);
    }

    public Flex bottom(float y) {
        return bottom(y, 0, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex bottom(float y, int offset) {
        return bottom(y, offset, 0, Unit.Measure.RELATIVE, true);
    }

    public Flex bottom(float y, float anchor) {
        return bottom(y, 0, anchor, Unit.Measure.RELATIVE, false);
    }

    public Flex bottom(float y, int offset, float anchor, Unit.Measure measure) {
        return bottom(y, offset, anchor, measure, false);
    }

    private Flex bottom(float y, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getBottom(), y, offset, anchor, measure, autoAnchor);
    }

    public Flex pos(int x, int y) {
        return left(x).top(y);
    }

    public Flex pos(float x, float y) {
        return left(x).top(y);
    }

    public Flex width(int w) {
        return width(w, Unit.Measure.PIXEL);
    }

    public Flex width(float w) {
        return width(w, Unit.Measure.RELATIVE);
    }

    public Flex width(float w, Unit.Measure measure) {
        return unitSize(getWidth(), w, measure);
    }

    public Flex height(int h) {
        return height(h, Unit.Measure.PIXEL);
    }

    public Flex height(float h) {
        return height(h, Unit.Measure.RELATIVE);
    }

    public Flex height(float h, Unit.Measure measure) {
        return unitSize(getHeight(), h, measure);
    }

    private Flex unitSize(Unit u, float val, Unit.Measure measure) {
        u.setValue(val);
        u.setMeasure(measure);
        return this;
    }

    public Flex size(int w, int h) {
        return width(w).height(h);
    }

    public Flex size(float w, float h) {
        return width(w).height(h);
    }

    public Flex anchorLeft(float val) {
        getLeft().setAnchor(val);
        getLeft().setAutoAnchor(false);
        return this;
    }

    public Flex anchorRight(float val) {
        getRight().setAnchor(1 - val);
        getRight().setAutoAnchor(false);
        return this;
    }

    public Flex anchorTop(float val) {
        getTop().setAnchor(val);
        getTop().setAutoAnchor(false);
        return this;
    }

    public Flex anchorBottom(float val) {
        getBottom().setAnchor(1 - val);
        getBottom().setAutoAnchor(false);
        return this;
    }

    public Flex anchor(Alignment alignment) {
        if (this.left != null) {
            anchorLeft(alignment.x);
        }
        if (this.right != null) {
            anchorRight(alignment.x);
        }
        if (this.top != null) {
            anchorTop(alignment.y);
        }
        if (this.bottom != null) {
            anchorBottom(alignment.y);
        }
        return this;
    }

    public Flex alignX(float val) {
        return left(val).anchorLeft(val);
    }

    public Flex alignY(float val) {
        return top(val).anchorTop(val);
    }

    public Flex align(Alignment alignment) {
        alignX(alignment.x);
        alignY(alignment.y);
        return this;
    }

    private Area getRelativeTo() {
        Area relativeTo = relativeToParent ? parent.getParentArea() : this.relativeTo;
        return relativeTo != null ? relativeTo : this.parent.getScreen().getViewport();
    }

    @Override
    public void apply(IGuiElement guiElement) {
        Area relativeTo = getRelativeTo();

        if (relativeTo.z() >= parent.getArea().z()) {
            GuiErrorHandler.INSTANCE.pushError(this.parent, "Widget can't be relative to a widget at the same level or above");
            return;
        }

        if (this.width != null && this.left != null && this.right != null)
            throw new IllegalStateException("Widget size/pos in x is over-specified");
        if (this.height != null && this.top != null && this.bottom != null)
            throw new IllegalStateException("Widget size/pos in y is over-specified");


        int w, h, x, y;
        if (this.left == null && this.right == null) {
            x = 0;
            w = this.width == null ? this.parent.getDefaultWidth() : calcWidth(this.width);
        } else {
            if (this.width == null) {
                if (this.left != null && this.right != null) {
                    x = calcX(this.left, -1);
                    int x2 = calcX(this.right, -1);
                    w = Math.abs(relativeTo.ex() - x2 - x - relativeTo.x);
                } else {
                    w = this.parent.getDefaultWidth();
                    if (this.left == null) {
                        x = calcX(this.right, w);
                        x -= w;
                    } else {
                        x = calcX(this.left, w);
                    }
                }
            } else if (right == null) {
                w = calcWidth(this.width);
                x = calcX(this.left, w);
            } else {
                w = calcWidth(this.width);
                x = calcX(this.right, w);
                x = relativeTo.w() - x - w;
            }
        }

        if (this.top == null && this.bottom == null) {
            y = 0;
            h = this.height == null ? this.parent.getDefaultHeight() : calcHeight(this.height);
        } else {
            if (this.height == null) {
                if (this.top != null && this.bottom != null) {
                    y = calcY(this.top, -1);
                    int y2 = calcY(this.bottom, -1);
                    h = Math.abs(relativeTo.ey() - y2 - y - relativeTo.y);
                } else {
                    h = this.parent.getDefaultHeight();
                    if (this.top == null) {
                        y = calcY(this.bottom, h);
                        y -= h;
                    } else {
                        y = calcY(this.top, h);
                    }
                }
            } else if (bottom == null) {
                h = calcHeight(this.height);
                y = calcY(this.top, h);
            } else {
                h = calcHeight(this.height);
                y = calcY(this.bottom, h);
                y = relativeTo.h() - y - h;
            }
        }

        /*TODO not that simple
        Box.SHARED.all(0);
        Box padding = this.parent.getPadding();
        Box margin = this.parent instanceof ModularPanel ? Box.SHARED : this.parent.getParent().getMargin();
        x += padding.left + margin.left;
        w -= padding.horizontal() + margin.horizontal();
        y += padding.top + margin.top;
        h -= padding.vertical() + margin.vertical();*/

        x += relativeTo.x;
        y += relativeTo.y;

        parent.getArea().set(x, y, w, h);
    }

    private int calcWidth(Unit w) {
        float val = w.getValue();
        if (w.isRelative()) {
            return (int) (val * getRelativeTo().width);
        }
        return (int) val;
    }

    public int calcHeight(Unit h) {
        float val = h.getValue();
        if (h.isRelative()) {
            return (int) (val * getRelativeTo().height);
        }
        return (int) val;
    }

    public int calcX(Unit x, int width) {
        float val = x.getValue();
        if (x.isRelative()) {
            Area relativeTo = getRelativeTo();
            val = relativeTo.width * val;
        }
        float anchor = x.getAnchor();
        if (width > 0 && anchor != 0) {
            val -= width * anchor;
        } else if (x.getOffset() != 0) {
            val += x.getOffset();
        }
        return (int) val;
    }

    public int calcY(Unit y, int height) {
        float val = y.getValue();
        if (y.isRelative()) {
            Area relativeTo = getRelativeTo();
            val = relativeTo.height * val;
        }
        float anchor = y.getAnchor();
        if (height > 0 && anchor != 0) {
            val -= height * anchor;
        } else if (y.getOffset() != 0) {
            val += y.getOffset();
        }
        return (int) val;
    }

    private Unit getLeft() {
        if (left == null) {
            Unit u = null;
            if (x.type == Unit.UNUSED) u = x;
            else if (x2.type == Unit.UNUSED) u = x2;
            else if (!defaultMode) {
                if (x.type == Unit.DEFAULT) u = x;
                else if (x2.type == Unit.DEFAULT) u = x2;
            }
            if (u == null) throw new IllegalStateException();
            u.type = defaultMode ? Unit.DEFAULT : Unit.LEFT;
            left = u;
        }
        return left;
    }

    private Unit getRight() {
        if (right == null) {
            Unit u = null;
            if (x.type == Unit.UNUSED) u = x;
            else if (x2.type == Unit.UNUSED) u = x2;
            else if (!defaultMode) {
                if (x.type == Unit.DEFAULT) u = x;
                else if (x2.type == Unit.DEFAULT) u = x2;
            }
            if (u == null) throw new IllegalStateException();
            u.type = defaultMode ? Unit.DEFAULT : Unit.RIGHT;
            right = u;
        }
        return right;
    }

    private Unit getTop() {
        if (top == null) {
            Unit u = null;
            if (y.type == Unit.UNUSED) u = y;
            else if (y2.type == Unit.UNUSED) u = y2;
            else if (!defaultMode) {
                if (y.type == Unit.DEFAULT) u = y;
                else if (y2.type == Unit.DEFAULT) u = y2;
            }
            if (u == null) {
                throw new IllegalStateException();
            }
            u.type = defaultMode ? Unit.DEFAULT : Unit.TOP;
            top = u;
        }
        return top;
    }

    private Unit getBottom() {
        if (bottom == null) {
            Unit u = null;
            if (y.type == Unit.UNUSED) u = y;
            else if (y2.type == Unit.UNUSED) u = y2;
            else if (!defaultMode) {
                if (y.type == Unit.DEFAULT) u = y;
                else if (y2.type == Unit.DEFAULT) u = y2;
            }
            if (u == null) {
                throw new IllegalStateException();
            }
            u.type = defaultMode ? Unit.DEFAULT : Unit.BOTTOM;
            bottom = u;
        }
        return bottom;
    }

    private Unit getWidth() {
        if (width == null) {
            Unit u = null;
            if (x.type == Unit.UNUSED) u = x;
            else if (x2.type == Unit.UNUSED) u = x2;
            else if (!defaultMode) {
                if (x.type == Unit.DEFAULT) u = x;
                else if (x2.type == Unit.DEFAULT) u = x2;
            }
            if (u == null) throw new IllegalStateException();
            u.type = defaultMode ? Unit.DEFAULT : Unit.WIDTH;
            width = u;
        }
        return width;
    }

    private Unit getHeight() {
        if (height == null) {
            Unit u = null;
            if (y.type == Unit.UNUSED) u = y;
            else if (y2.type == Unit.UNUSED) u = y2;
            else if (!defaultMode) {
                if (y.type == Unit.DEFAULT) u = y;
                else if (y2.type == Unit.DEFAULT) u = y2;
            }
            if (u == null) {
                throw new IllegalStateException();
            }
            u.type = defaultMode ? Unit.DEFAULT : Unit.HEIGHT;
            height = u;
        }
        return height;
    }
}
