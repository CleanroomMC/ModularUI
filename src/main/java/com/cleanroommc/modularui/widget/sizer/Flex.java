package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.GuiError;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.layout.IResizeable;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IPositioned;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.Alignment;

import net.minecraft.inventory.Slot;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * This class handles resizing and positioning of widgets.
 */
public class Flex implements IResizeable, IPositioned<Flex> {

    private final DimensionSizer x = new DimensionSizer(GuiAxis.X);
    private final DimensionSizer y = new DimensionSizer(GuiAxis.Y);
    private boolean expanded = false;
    private final IGuiElement parent;
    private Area relativeTo;
    private boolean relativeToParent = true;

    public Flex(IGuiElement parent) {
        this.parent = parent;
    }

    public void reset() {
        this.x.reset();
        this.y.reset();
    }

    public void resetPosition() {
        this.x.resetPosition();
        this.y.resetPosition();
    }

    public Flex startDefaultMode() {
        this.x.setDefaultMode(true);
        this.y.setDefaultMode(true);
        return this;
    }

    public Flex endDefaultMode() {
        this.x.setDefaultMode(false);
        this.y.setDefaultMode(false);
        return this;
    }

    @Override
    public Flex flex() {
        return this;
    }

    @Override
    public Area getArea() {
        return this.parent.getArea();
    }

    @Override
    public boolean isXCalculated() {
        return this.x.isPosCalculated();
    }

    @Override
    public boolean isYCalculated() {
        return this.y.isPosCalculated();
    }

    @Override
    public boolean isWidthCalculated() {
        return this.x.isSizeCalculated();
    }

    @Override
    public boolean isHeightCalculated() {
        return this.y.isSizeCalculated();
    }

    public Flex coverChildrenWidth() {
        this.x.setCoverChildren(true);
        return this;
    }

    public Flex coverChildrenHeight() {
        this.y.setCoverChildren(true);
        return this;
    }

    public Flex cancelMovementX() {
        this.x.setCancelAutoMovement(true);
        return this;
    }

    public Flex cancelMovementY() {
        this.y.setCancelAutoMovement(true);
        return this;
    }

    public Flex expanded() {
        this.expanded = true;
        return this;
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

    @ApiStatus.Internal
    public Flex left(float x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getLeft(), x, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public Flex left(DoubleSupplier x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getLeft(), x, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public Flex right(float x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getRight(), x, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public Flex right(DoubleSupplier x, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getRight(), x, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public Flex top(float y, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getTop(), y, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public Flex top(DoubleSupplier y, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getTop(), y, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public Flex bottom(float y, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getBottom(), y, offset, anchor, measure, autoAnchor);
    }

    @ApiStatus.Internal
    public Flex bottom(DoubleSupplier y, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        return unit(getBottom(), y, offset, anchor, measure, autoAnchor);
    }

    private Flex unit(Unit u, float val, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        u.setValue(val);
        u.setMeasure(measure);
        u.setOffset(offset);
        u.setAnchor(anchor);
        u.setAutoAnchor(autoAnchor);
        return this;
    }

    private Flex unit(Unit u, DoubleSupplier val, int offset, float anchor, Unit.Measure measure, boolean autoAnchor) {
        u.setValue(val);
        u.setMeasure(measure);
        u.setOffset(offset);
        u.setAnchor(anchor);
        u.setAutoAnchor(autoAnchor);
        return this;
    }

    public Flex width(float w, Unit.Measure measure) {
        return unitSize(getWidth(), w, measure);
    }

    public Flex width(DoubleSupplier w, Unit.Measure measure) {
        return unitSize(getWidth(), w, measure);
    }

    public Flex height(float h, Unit.Measure measure) {
        return unitSize(getHeight(), h, measure);
    }

    public Flex height(DoubleSupplier h, Unit.Measure measure) {
        return unitSize(getHeight(), h, measure);
    }

    private Flex unitSize(Unit u, float val, Unit.Measure measure) {
        u.setValue(val);
        u.setMeasure(measure);
        return this;
    }

    private Flex unitSize(Unit u, DoubleSupplier val, Unit.Measure measure) {
        u.setValue(val);
        u.setMeasure(measure);
        return this;
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
        if (this.x.hasStart()) {
            anchorLeft(alignment.x);
        }
        if (this.x.hasEnd()) {
            anchorRight(alignment.x);
        }
        if (this.y.hasStart()) {
            anchorTop(alignment.y);
        }
        if (this.y.hasEnd()) {
            anchorBottom(alignment.y);
        }
        return this;
    }

    private IResizeable getRelativeTo() {
        IGuiElement parent = this.parent.getParent();
        IResizeable relativeTo = this.relativeToParent && parent != null ? parent.resizer() : this.relativeTo;
        return relativeTo != null ? relativeTo : this.parent.getScreen().getScreenArea();
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public boolean hasYPos() {
        return this.y.hasPos();
    }

    public boolean hasXPos() {
        return this.x.hasPos();
    }

    public boolean hasHeight() {
        return this.y.hasSize();
    }

    public boolean hasWidth() {
        return this.x.hasSize();
    }

    public boolean xAxisDependsOnChildren() {
        return this.x.dependsOnChildren();
    }

    public boolean yAxisDependsOnChildren() {
        return this.y.dependsOnChildren();
    }

    public boolean hasFixedSize() {
        return this.x.hasFixedSize() && this.y.hasFixedSize();
    }

    @Override
    public void initResizing() {
        setMarginPaddingApplied(false);
        setResized(false);
    }

    @Override
    public void setResized(boolean x, boolean y, boolean w, boolean h) {
        this.x.setResized(x, w);
        this.y.setResized(y, h);
    }

    @Override
    public void setXMarginPaddingApplied(boolean b) {
        this.x.setMarginPaddingApplied(b);
    }

    @Override
    public void setYMarginPaddingApplied(boolean b) {
        this.y.setMarginPaddingApplied(b);
    }

    @Override
    public boolean isXMarginPaddingApplied() {
        return this.x.isMarginPaddingApplied();
    }

    @Override
    public boolean isYMarginPaddingApplied() {
        return this.y.isMarginPaddingApplied();
    }

    @Override
    public boolean resize(IGuiElement guiElement) {
        IResizeable relativeTo = getRelativeTo();
        Area relativeArea = relativeTo.getArea();
        byte panelLayer = this.parent.getArea().getPanelLayer();

        if (relativeArea.getPanelLayer() > panelLayer ||
                (relativeArea.getPanelLayer() == panelLayer && relativeArea.z() >= this.parent.getArea().z())) {
            GuiError.throwNew(this.parent, GuiError.Type.SIZING, "Widget can't be relative to a widget at the same level or above");
            return true;
        }

        // calculate x, y, width and height if possible
        this.x.apply(guiElement.getArea(), relativeTo, guiElement::getDefaultWidth);
        this.y.apply(guiElement.getArea(), relativeTo, guiElement::getDefaultHeight);
        return isFullyCalculated();
    }

    @Override
    public boolean postResize(IGuiElement guiElement) {
        if (!this.x.dependsOnChildren() && !this.y.dependsOnChildren()) return isFullyCalculated();
        if (this.parent instanceof ILayoutWidget) {
            // layout widgets handle widget layout's themselves, so we only need to fit the right and bottom border
            coverChildrenForLayout();
            return isFullyCalculated();
        }
        // non layout widgets can have their children in any position
        // we try to wrap all edges as close as possible to all widgets
        // this means for each edge there is at least one widget that touches it (plus padding and margin)

        // children are now calculated and now this area can be calculated if it requires childrens area
        List<IWidget> children = ((IWidget) this.parent).getChildren();
        if (!children.isEmpty()) {
            int moveChildrenX = 0, moveChildrenY = 0;

            Box padding = this.parent.getArea().getPadding();
            // first calculate the area the children span
            int x0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, y0 = Integer.MAX_VALUE, y1 = Integer.MIN_VALUE;
            int w = 0, h = 0;
            for (IWidget child : children) {
                Box margin = child.getArea().getMargin();
                IResizeable resizeable = child.resizer();
                Area area = child.getArea();
                if (this.x.dependsOnChildren() && resizeable.isWidthCalculated()) {
                    // minimum width this widget requests
                    w = Math.max(w, area.requestedWidth() + padding.horizontal());
                    if (resizeable.isXCalculated()) {
                        // if pos is calculated use that
                        x0 = Math.min(x0, area.rx - padding.left - margin.left);
                        x1 = Math.max(x1, area.rx + area.width + padding.right + margin.right);
                    }
                }
                if (this.y.dependsOnChildren() && resizeable.isHeightCalculated()) {
                    h = Math.max(h, area.requestedHeight() + padding.vertical());
                    if (resizeable.isYCalculated()) {
                        y0 = Math.min(y0, area.ry - padding.top - margin.top);
                        y1 = Math.max(y1, area.ry + area.height + padding.bottom + margin.bottom);
                    }
                }
            }
            if (x1 == Integer.MIN_VALUE) x1 = 0;
            if (y1 == Integer.MIN_VALUE) y1 = 0;
            if (x0 == Integer.MAX_VALUE) x0 = 0;
            if (y0 == Integer.MAX_VALUE) y0 = 0;
            if (w > x1 - x0)
                x1 = x0 + w; // we found at least one widget which was wider than what was calculated by start and end pos
            if (h > y1 - y0) y1 = y0 + h;

            // now calculate new x, y, width and height based on the childrens area
            Area relativeTo = getRelativeTo().getArea();
            if (this.x.dependsOnChildren()) {
                // apply the size to this widget
                // the return value is the amount of pixels we need to move the children
                moveChildrenX = this.x.postApply(this.parent.getArea(), relativeTo, x0, x1);
            }
            if (this.y.dependsOnChildren()) {
                moveChildrenY = this.y.postApply(this.parent.getArea(), relativeTo, y0, y1);
            }
            // since the edges might have been moved closer to the widgets, the widgets should move back into it's original (absolute) position
            if (moveChildrenX != 0 || moveChildrenY != 0) {
                for (IWidget widget : children) {
                    Area area = widget.getArea();
                    IResizeable resizeable = widget.resizer();
                    if (resizeable.isXCalculated()) area.rx += moveChildrenX;
                    if (resizeable.isYCalculated()) area.ry += moveChildrenY;
                }
            }
        }
        return isFullyCalculated();
    }

    private void coverChildrenForLayout() {
        List<IWidget> children = ((IWidget) this.parent).getChildren();
        if (!children.isEmpty()) {
            Box padding = this.parent.getArea().getPadding();
            // first calculate the area the children span
            int x1 = Integer.MIN_VALUE, y1 = Integer.MIN_VALUE;
            int w = 0, h = 0;
            for (IWidget child : children) {
                Box margin = child.getArea().getMargin();
                IResizeable resizeable = child.resizer();
                Area area = child.getArea();
                if (this.x.dependsOnChildren() && resizeable.isWidthCalculated()) {
                    w = Math.max(w, area.requestedWidth() + padding.horizontal());
                    if (resizeable.isXCalculated()) {
                        x1 = Math.max(x1, area.rx + area.width + padding.right + margin.right);
                    }
                }
                if (this.y.dependsOnChildren() && resizeable.isHeightCalculated()) {
                    h = Math.max(h, area.requestedHeight() + padding.vertical());
                    if (resizeable.isXCalculated()) {
                        y1 = Math.max(y1, area.ry + area.height + padding.bottom + margin.bottom);
                    }
                }
            }
            if (x1 == Integer.MIN_VALUE) x1 = 0;
            if (y1 == Integer.MIN_VALUE) y1 = 0;
            if (w > x1) x1 = w;
            if (h > y1) y1 = h;

            Area relativeTo = getRelativeTo().getArea();
            if (this.x.dependsOnChildren()) {
                this.x.postApply(getArea(), relativeTo, 0, x1);
            }
            if (this.y.dependsOnChildren()) {
                this.y.postApply(getArea(), relativeTo, 0, y1);
            }
        }
    }

    @Override
    public void applyPos(IGuiElement parent) {
        Area relativeTo = getRelativeTo().getArea();
        Area area = parent.getArea();
        // apply margin and padding if not done yet
        this.x.applyMarginAndPaddingToPos(area, relativeTo);
        this.y.applyMarginAndPaddingToPos(area, relativeTo);
        // after all widgets x, y, width and height have been calculated we can now calculate the absolute position
        area.applyPos(relativeTo.x, relativeTo.y);
        Area parentArea = parent.getParentArea();
        area.rx = area.x - parentArea.x;
        area.ry = area.y - parentArea.y;
        if (parent instanceof IVanillaSlot vanillaSlot) {
            // special treatment for minecraft slots
            Slot slot = vanillaSlot.getVanillaSlot();
            slot.xPos = parent.getArea().x;
            slot.yPos = parent.getArea().y;
        }
    }

    private Unit getLeft() {
        return this.x.getStart();
    }

    private Unit getRight() {
        return this.x.getEnd();
    }

    private Unit getTop() {
        return this.y.getStart();
    }

    private Unit getBottom() {
        return this.y.getEnd();
    }

    private Unit getWidth() {
        return this.x.getSize();
    }

    private Unit getHeight() {
        return this.y.getSize();
    }
}