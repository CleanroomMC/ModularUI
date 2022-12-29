package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.layout.IViewport;

import java.util.List;

public interface IWidgetList {

    void add(IWidget widget, List<IViewport> viewports);

    IWidget peek();

    boolean isEmpty();
}
