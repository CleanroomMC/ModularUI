package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.widget.IWidget;

import java.util.List;

public interface WidgetNode<T> {

    IWidget getWidget();

    T getParent();

    List<T> getChildren();
}
