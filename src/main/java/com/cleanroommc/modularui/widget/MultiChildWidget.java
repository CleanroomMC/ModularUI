package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.api.IWidgetParent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MultiChildWidget extends Widget implements IWidgetParent {

    private final List<Widget> children = new ArrayList<>();

    public MultiChildWidget addChild(Widget widget) {
        if(widget == this) {
            ModularUIMod.LOGGER.error("Can't add self!");
            return this;
        }
        if (isInitialised()) {
            ModularUIMod.LOGGER.error("Can't add child after initialised!");
        } else {
            children.add(widget);
        }
        return this;
    }

    @Override
    public List<Widget> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
