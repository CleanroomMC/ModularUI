package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.layout.IResizeable2;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ResizeNode implements IResizeable2 {

    private ResizeNode defaultParent;
    private ResizeNode parentOverride;
    private final List<ResizeNode> children = new ArrayList<>();
    private boolean requiresResize = true;

    @ApiStatus.Internal
    public List<ResizeNode> getChildren() {
        return children;
    }

    public ResizeNode getParent() {
        return parentOverride != null ? parentOverride : defaultParent;
    }

    public void dispose() {
        this.defaultParent = null;
        this.parentOverride = null;
        this.children.clear();
    }

    private boolean removeFromParent(ResizeNode parent, ResizeNode parent2, ResizeNode replacement) {
        if (parent != null) {
            if (parent == replacement) return true;
            parent.children.remove(this);
        } else if (parent2 != null) {
            if (parent2 == replacement) return true;
            parent2.children.remove(this);
        }
        return false;
    }

    public void setDefaultParent(ResizeNode resizeNode) {
        if (removeFromParent(this.defaultParent, null, resizeNode)) return;
        this.defaultParent = resizeNode;
        if (resizeNode != null) {
            resizeNode.children.add(this);
        }
    }

    protected void setParentOverride(ResizeNode resizeNode) {
        if (removeFromParent(this.parentOverride, this.defaultParent, resizeNode)) return;
        this.parentOverride = resizeNode;
        if (this.parentOverride != null) {
            this.parentOverride.children.add(this);
        } else if (this.defaultParent != null) {
            this.defaultParent.children.add(this);
        }
    }

    @Override
    public void initResizing(boolean onOpen) {
        reset();
    }

    public void reset() {}

    public void markDirty() {
        this.requiresResize = true;
    }

    public void onResized() {
        this.requiresResize = false;
    }

    public boolean requiresResize() {
        return this.requiresResize;
    }

    public boolean dependsOnParentX() {
        return false;
    }

    public boolean dependsOnParentY() {
        return false;
    }

    public boolean dependsOnParent() {
        return dependsOnParentX() || dependsOnParentY();
    }

    public boolean dependsOnParent(GuiAxis axis) {
        return axis.isHorizontal() ? dependsOnParentX() : dependsOnParentY();
    }

    public boolean dependsOnChildrenX() {
        return false;
    }

    public boolean dependsOnChildrenY() {
        return false;
    }

    public boolean dependsOnChildren() {
        return dependsOnChildrenX() || dependsOnChildrenY();
    }

    public boolean dependsOnChildren(GuiAxis axis) {
        return axis.isHorizontal() ? dependsOnChildrenX() : dependsOnChildrenY();
    }

    public boolean isSameResizer(ResizeNode node) {
        return node == this;
    }

    public boolean isLayout() {
        return false;
    }

    public boolean layoutChildren() {
        return true;
    }

    public boolean postLayoutChildren() {
        return true;
    }

    @ApiStatus.Internal
    public void checkExpanded(@Nullable GuiAxis axis) {}

    public abstract boolean hasYPos();

    public abstract boolean hasXPos();

    public abstract boolean hasHeight();

    public abstract boolean hasWidth();

    public abstract boolean hasStartPos(GuiAxis axis);

    public abstract boolean hasEndPos(GuiAxis axis);

    public boolean hasPos(GuiAxis axis) {
        return axis.isHorizontal() ? hasXPos() : hasYPos();
    }

    public boolean hasSize(GuiAxis axis) {
        return axis.isHorizontal() ? hasWidth() : hasHeight();
    }

    public boolean isExpanded() {
        return false;
    }

    public abstract boolean isFullSize();

    public abstract boolean hasFixedSize();

    public abstract String getDebugDisplayName();
}
