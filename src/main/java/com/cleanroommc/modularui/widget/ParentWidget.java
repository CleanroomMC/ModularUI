package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.widget.IWidget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class ParentWidget<W extends ParentWidget<W>> extends Widget<W> {

    private final List<IWidget> children = new ArrayList<>();

    @NotNull
    @Override
    public List<IWidget> getChildren() {
        return children;
    }

    public boolean addChild(IWidget child, int index) {
        if (child == null || child == this || getChildren().contains(child)) {
            return false;
        }
        if (index < 0) {
            index = getChildren().size() + index + 1;
        }
        this.children.add(index, child);
        if (isValid()) {
            child.initialise(this);
        }
        onChildAdd(child);
        return true;
    }

    public boolean remove(IWidget child) {
        if (this.children.remove(child)) {
            child.dispose();
            onChildRemove(child);
            return true;
        }
        return false;
    }

    public boolean remove(int index) {
        if (index < 0) {
            index = getChildren().size() + index + 1;
        }
        IWidget child = this.children.remove(index);
        child.dispose();
        onChildRemove(child);
        return true;
    }

    public void onChildAdd(IWidget child) {
    }

    public void onChildRemove(IWidget child) {
    }

    public W child(IWidget child) {
        if (!addChild(child, -1)) {
            throw new IllegalStateException("Failed to add child");
        }
        return getThis();
    }
}
