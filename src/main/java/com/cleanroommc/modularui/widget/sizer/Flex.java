package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.GuiErrorHandler;
import com.cleanroommc.modularui.api.IGuiElement;
import com.cleanroommc.modularui.api.IVanillaSlot;
import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.utils.Alignment;
import net.minecraft.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public class Flex implements IResizeable {

    private final Unit x = new Unit();
    private final Unit y = new Unit();
    private final Unit x2 = new Unit();
    private final Unit y2 = new Unit();
    private final IGuiElement parent;
    private Unit left, right, top, bottom, width, height;
    private Area relativeTo;
    private boolean relativeToParent = true;
    private boolean defaultMode = false;
    private boolean skip = false;

    private int relativeX, relativeY;

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

    public Flex coverChildrenWidth() {
        getWidth().setCoverChildren(true);
        return this;
    }

    public Flex coverChildrenHeight() {
        getHeight().setCoverChildren(true);
        return this;
    }

    public Flex coverChildren() {
        return coverChildrenWidth().coverChildrenHeight();
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

    @ApiStatus.Internal
    public void setRelativeX(int x) {
        this.relativeX = x;
    }

    @ApiStatus.Internal
    public void setRelativeY(int y) {
        this.relativeY = y;
    }

    public boolean hasYPos() {
        return this.top != null || this.bottom != null;
    }

    public boolean hasXPos() {
        return this.left != null || this.right != null;
    }

    public boolean doCoverChildrenHeight() {
        return this.height != null && this.height.isCoverChildren();
    }

    public boolean doCoverChildrenWidth() {
        return this.width != null && this.width.isCoverChildren();
    }

    public boolean dependsOnChildren() {
        return (this.width != null && this.width.dependsOnChildren()) ||
                (this.width != null && this.width.dependsOnChildren()) ||
                (this.left != null && this.left.dependsOnChildren()) ||
                (this.right != null && this.right.dependsOnChildren()) ||
                (this.top != null && this.top.dependsOnChildren()) ||
                (this.bottom != null && this.bottom.dependsOnChildren());
    }

    public boolean dependsOnParent() {
        return (this.width != null && this.width.dependsOnParent()) ||
                (this.height != null && this.height.dependsOnParent()) ||
                (this.left != null && this.left.dependsOnParent()) ||
                (this.right != null) ||
                (this.top != null && this.top.dependsOnParent()) ||
                (this.bottom != null);
    }

    @ApiStatus.Internal
    public void skip() {
        this.skip = true;
    }

    @Override
    public boolean isSkip() {
        return skip;
    }

    @Override
    public void apply(IGuiElement guiElement) {
        if (isSkip()) return;
        Area relativeTo = getRelativeTo();

        if (relativeTo.z() >= parent.getArea().z()) {
            GuiErrorHandler.INSTANCE.pushError(this.parent, "Widget can't be relative to a widget at the same level or above");
            return;
        }
        boolean dependsOnChildren = dependsOnChildren();
        if (this.width != null && this.left != null && this.right != null) {
            throw new IllegalStateException("Widget size/pos in x is over-specified");
        }
        if (this.height != null && this.top != null && this.bottom != null) {
            throw new IllegalStateException("Widget size/pos in y is over-specified");
        }

        int w, h, x, y;

        if (dependsOnChildren) {
            if (!(this.parent instanceof IWidget)) {
                throw new IllegalStateException("Can only cover children if instance of IWidget");
            }

            IWidget widget = (IWidget) this.parent;

            List<IWidget> children = widget.getChildren();
            if (!children.isEmpty()) {
                for (IWidget child : children) {
                    if (dependsOnThis(child)) {
                        //throw new IllegalStateException("Children can't depend on their parent if the parent wants to cover it's children");
                        child.flex().skip();
                    }
                }
            }
        }

        // calc left, right and width
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

        // calc top, bottom and height
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

        // apply padding and margin
        Box.SHARED.all(0);
        Box padding = relativeTo.getPadding();
        Box margin = this.parent.getArea().getMargin();
        int parentWidth = relativeTo.width;
        int parentHeight = relativeTo.height;

        if (parentWidth < 1 /*|| (this.width != null && !this.width.isRelative())*/) {
            this.relativeX = x;
        } else {
            this.relativeX = Math.max(x, padding.left + margin.left);
            w = Math.min(w, parentWidth - padding.horizontal() - margin.horizontal());
        }

        if (parentHeight < 1 /*|| (this.height != null && !this.height.isRelative())*/) {
            this.relativeY = y;
        } else {
            this.relativeY = Math.max(y, padding.top + margin.top);
            w = Math.min(w, parentHeight - padding.vertical() - margin.vertical());
        }

        x += relativeTo.x;
        y += relativeTo.y;

        parent.getArea().set(x, y, w, h);
    }

    @Override
    public void postApply(IGuiElement guiElement) {
        List<IWidget> children = ((IWidget) parent).getChildren();
        if (!children.isEmpty()) {
            int moveChildrenX = 0, moveChildrenY = 0;

            if (doCoverChildrenWidth() || doCoverChildrenHeight()) {
                Box padding = this.parent.getArea().getPadding();
                // calculate the area the children span
                int x0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y0 = Integer.MAX_VALUE, y1 = Integer.MIN_VALUE;
                for (IWidget child : children) {
                    Box margin = child.getArea().getMargin();
                    Flex flex = child.flex();
                    Area area = child.getArea();
                    x0 = Math.min(x0, flex.relativeX - padding.left - margin.left);
                    x1 = Math.max(x1, flex.relativeX + area.width + padding.right + margin.right);
                    y0 = Math.min(y0, flex.relativeY - padding.top - margin.top);
                    y1 = Math.max(y1, flex.relativeY + area.height + padding.bottom + margin.bottom);
                }

                Area relativeTo = getRelativeTo();
                if (doCoverChildrenWidth()) {
                    // calculate width and recalculate x based on the new width
                    int w = x1 - x0, x;
                    parent.getArea().width = w;
                    if (left != null) {
                        x = calcX(this.left, w);
                    } else if (right != null) {
                        x = calcX(this.right, w);
                        x = relativeTo.w() - x - w;
                    } else {
                        x = this.relativeX + x0 + this.parent.getArea().getMargin().left;
                        moveChildrenX = -x0;
                    }
                    this.relativeX = x;
                }
                if (doCoverChildrenHeight()) {
                    // calculate height and recalculate y based on the new height
                    int h = y1 - y0, y;
                    parent.getArea().height = h;
                    if (top != null) {
                        y = calcY(this.top, h);
                    } else if (bottom != null) {
                        y = calcY(this.bottom, h);
                        y = relativeTo.h() - y - h;
                    } else {
                        y = this.relativeY + y0 + this.parent.getArea().getMargin().top;
                        moveChildrenY = -y0;
                    }
                    this.relativeY = y;
                }
            }
            for (IWidget widget : children) {
                if (widget.flex().isSkip()) {
                    widget.flex().skip = false;
                    widget.resize();
                } else {
                    Flex flex = widget.flex();
                    flex.relativeX += moveChildrenX;
                    flex.relativeY += moveChildrenY;
                }
            }
        }
    }

    @Override
    public void applyPos(IGuiElement parent) {
        Area relativeTo = getRelativeTo();
        parent.getArea().x = relativeTo.x + this.relativeX;
        parent.getArea().y = relativeTo.y + this.relativeY;
        if (parent instanceof IVanillaSlot) {
            Slot slot = ((IVanillaSlot) parent).getVanillaSlot();
            slot.xPos = parent.getArea().x;
            slot.yPos = parent.getArea().y;
        }
    }

    private boolean dependsOnThis(IWidget child) {
        Flex flex = child.getFlex();
        if (flex == null || flex.getRelativeTo() != this.parent.getArea()) return false;
        return flex.dependsOnParent();
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
                if (right.type == Unit.DEFAULT) {
                    u = right;
                    right = null;
                } else if (width.type == Unit.DEFAULT) {
                    u = width;
                    width = null;
                }
            }
            if (u == null) throw new IllegalStateException();
            left = u;
            left.reset();
        }
        left.type = defaultMode ? Unit.DEFAULT : Unit.LEFT;
        return left;
    }

    private Unit getRight() {
        if (right == null) {
            Unit u = null;
            if (x.type == Unit.UNUSED) u = x;
            else if (x2.type == Unit.UNUSED) u = x2;
            else if (!defaultMode) {
                if (left.type == Unit.DEFAULT) {
                    u = left;
                    left = null;
                } else if (width.type == Unit.DEFAULT) {
                    u = width;
                    width = null;
                }
            }
            if (u == null) throw new IllegalStateException();
            right = u;
            right.reset();
        }
        right.type = defaultMode ? Unit.DEFAULT : Unit.RIGHT;
        return right;
    }

    private Unit getTop() {
        if (top == null) {
            Unit u = null;
            if (y.type == Unit.UNUSED) u = y;
            else if (y2.type == Unit.UNUSED) u = y2;
            else if (!defaultMode) {
                if (bottom.type == Unit.DEFAULT) {
                    u = bottom;
                    bottom = null;
                } else if (height.type == Unit.DEFAULT) {
                    u = height;
                    height = null;
                }
            }
            if (u == null) {
                throw new IllegalStateException();
            }
            top = u;
            top.reset();
        }
        top.type = defaultMode ? Unit.DEFAULT : Unit.TOP;
        return top;
    }

    private Unit getBottom() {
        if (bottom == null) {
            Unit u = null;
            if (y.type == Unit.UNUSED) u = y;
            else if (y2.type == Unit.UNUSED) u = y2;
            else if (!defaultMode) {
                if (top.type == Unit.DEFAULT) {
                    u = top;
                    top = null;
                } else if (height.type == Unit.DEFAULT) {
                    u = height;
                    height = null;
                }
            }
            if (u == null) {
                throw new IllegalStateException();
            }
            bottom = u;
            bottom.reset();
        }
        bottom.type = defaultMode ? Unit.DEFAULT : Unit.BOTTOM;
        return bottom;
    }

    private Unit getWidth() {
        if (width == null) {
            Unit u = null;
            if (x.type == Unit.UNUSED) u = x;
            else if (x2.type == Unit.UNUSED) u = x2;
            else if (!defaultMode) {
                if (right.type == Unit.DEFAULT) {
                    u = right;
                    right = null;
                } else if (left.type == Unit.DEFAULT) {
                    u = left;
                    left = null;
                }
            }
            if (u == null) throw new IllegalStateException();
            width = u;
            width.reset();
        }
        width.type = defaultMode ? Unit.DEFAULT : Unit.WIDTH;
        return width;
    }

    private Unit getHeight() {
        if (height == null) {
            Unit u = null;
            if (y.type == Unit.UNUSED) u = y;
            else if (y2.type == Unit.UNUSED) u = y2;
            else if (!defaultMode) {
                if (bottom.type == Unit.DEFAULT) {
                    u = bottom;
                    bottom = null;
                } else if (top.type == Unit.DEFAULT) {
                    u = top;
                    top = null;
                }
            }
            if (u == null) {
                throw new IllegalStateException();
            }
            height = u;
            height.reset();
        }
        height.type = defaultMode ? Unit.DEFAULT : Unit.HEIGHT;
        return height;
    }
}
