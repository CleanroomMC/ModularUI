package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IResizeable;
import com.cleanroommc.modularui.api.widget.IDelegatingWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Flex;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DelegatingSingleChildWidget<W extends SingleChildWidget<W>> extends SingleChildWidget<W> implements IDelegatingWidget {

    private boolean currentlyResizing = false;

    @Override
    public void onInit() {
        super.onInit();
        if (hasChildren()) getChild().flex().relative(getParent());
    }

    @Override
    protected void onChildAdd(IWidget child) {
        super.onChildAdd(child);
    }

    @Override
    public void beforeResize(boolean onOpen) {
        this.currentlyResizing = true;
        super.beforeResize(onOpen);
        if (getDelegate() != null) getDelegate().beforeResize(onOpen);
    }

    @Override
    public void onResized() {
        super.onResized();
        if (getDelegate() != null) getDelegate().onResized();
    }

    @Override
    public void postResize() {
        super.postResize();
        if (getDelegate() != null) getDelegate().postResize();
        this.currentlyResizing = false;
        Area childArea = getChild().getArea();
        Area area = super.getArea();
        area.set(childArea);
        area.rx = childArea.rx;
        area.ry = childArea.ry;
        childArea.x = 0;
        childArea.y = 0;
        childArea.rx = 0;
        childArea.ry = 0;
    }

    @Override
    public Flex getFlex() {
        return getDelegate() != null ? getDelegate().getFlex() : super.getFlex();
    }

    @Override
    public @NotNull IResizeable resizer() {
        return getDelegate() != null ? getDelegate().resizer() : super.resizer();
    }

    @Override
    public Area getArea() {
        return getDelegate() != null && this.currentlyResizing ? getDelegate().getArea() : super.getArea();
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        return getDelegate() != null && this.currentlyResizing ? getDelegate().getChildren() : super.getChildren();
    }

    @Override
    public boolean requiresResize() {
        return super.requiresResize() || (getDelegate() != null && getDelegate().requiresResize());
    }

    @Override
    public int getDefaultWidth() {
        return getDelegate() != null ? getDelegate().getDefaultWidth() : super.getDefaultWidth();
    }

    @Override
    public int getDefaultHeight() {
        return getDelegate() != null ? getDelegate().getDefaultHeight() : super.getDefaultHeight();
    }

    @Override
    public IWidget getDelegate() {
        return getChild();
    }
}
