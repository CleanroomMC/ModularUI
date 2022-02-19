package io.github.cleanroommc.modularui.widget;

import io.github.cleanroommc.modularui.api.IWidgetParent;
import io.github.cleanroommc.modularui.api.math.GuiArea;

import java.util.ArrayList;
import java.util.List;

public abstract class WidgetGroup extends Widget implements IWidgetParent {

    private final List<Widget> children = new ArrayList<>();

    public WidgetGroup(GuiArea guiArea) {
        super(guiArea);
    }

    public WidgetGroup(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    @Override
    public List<Widget> getChildren() {
        return children;
    }
}
