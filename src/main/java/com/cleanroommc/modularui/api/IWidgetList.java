package com.cleanroommc.modularui.api;

import java.util.List;

public interface IWidgetList {

    void add(IWidget widget, List<IViewport> viewports);

    IWidget peek();

    boolean isEmpty();
}
