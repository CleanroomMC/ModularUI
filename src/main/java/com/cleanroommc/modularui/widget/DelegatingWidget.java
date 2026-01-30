package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IDelegatingWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.StandardResizer;

import org.jetbrains.annotations.NotNull;

public class DelegatingWidget extends AbstractWidget implements IDelegatingWidget {

    private IWidget delegate;

    public DelegatingWidget(IWidget delegate) {
        this.delegate = delegate;
    }

    protected void setDelegate(IWidget delegate) {
        if (this.delegate != null) {
            this.delegate.dispose();
        }
        this.delegate = delegate;
        if (this.delegate != null && isValid()) {
            initialise(getParent(), true);
            delegate.scheduleResize();
        }
        onChangeDelegate(delegate);
    }

    protected void onChangeDelegate(IWidget delegate) {}

    @Override
    public void afterInit() {
        super.resizer().setDefaultParent(null); // remove this widget from the resize node tree
        if (hasChildren()) {
            getDelegate().resizer().setDefaultParentIsDelegating(true);
            getDelegate().resizer().relative(getParent()); // add the delegated widget at the place of this widget on the resize node tree
        }
    }

    @Override
    public void postResize() {
        super.postResize();
        if (getDelegate() != null) {
            Area childArea = getDelegate().getArea();
            Area area = super.getArea();
            area.set(childArea);
            area.rx = childArea.rx;
            area.ry = childArea.ry;
            childArea.rx = 0;
            childArea.ry = 0;
        }
    }

    @Override
    public @NotNull StandardResizer resizer() {
        return getDelegate() != null ? getDelegate().resizer() : super.resizer();
    }

    @Override
    public Area getArea() {
        return getDelegate() != null ? getDelegate().getArea() : super.getArea();
    }

    @Override
    public void transform(IViewportStack stack) {
        stack.translate(super.getArea().rx, super.getArea().ry, 0);
    }

    @Override
    public boolean canBeSeen(IViewportStack stack) {
        return false;
    }

    @Override
    public boolean requiresResize() {
        return getDelegate() != null && getDelegate().requiresResize();
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
        return delegate;
    }
}
