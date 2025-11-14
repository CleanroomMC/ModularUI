package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.layout.IResizeable2;

import java.util.ArrayList;
import java.util.List;

public abstract class ResizeNode implements IResizeable2 {

    private ResizeNode parent;
    private final List<ResizeNode> children = new ArrayList<>();
    private boolean requiresResize = true;

    public ResizeNode getParent() {
        return parent;
    }

    public List<ResizeNode> getChildren() {
        return children;
    }

    public void setParent(ResizeNode resizeNode) {
        if (this.parent != null) {
            if (this.parent == resizeNode) return;
            this.parent.children.remove(this);
        }
        this.parent = resizeNode;
        if (resizeNode != null) {
            resizeNode.children.add(this);
        }
    }

    public void reset() {
        initResizing();
        this.parent = null;
        this.children.clear();
    }

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
}
