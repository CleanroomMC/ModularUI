package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.api.widget.IWidget;

import java.util.Objects;

public abstract class WidgetResizeNode extends ResizeNode {

    private final IWidget widget;

    protected WidgetResizeNode(IWidget widget) {
        this.widget = Objects.requireNonNull(widget);
    }

    public IWidget getWidget() {
        return widget;
    }

    @Override
    public Area getArea() {
        return widget.getArea();
    }
}
