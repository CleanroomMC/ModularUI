package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.screen.viewport.TransformationMatrix;

public interface IWidgetList {

    void add(IWidget widget, TransformationMatrix viewports);

    IWidget peek();

    boolean isEmpty();

    int size();
}
