package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.math.Size;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiChildWidget extends Widget implements IWidgetParent {

    protected final List<Widget> children = new ArrayList<>();

    public MultiChildWidget addChild(Widget widget) {
        if (widget == null) {
            ModularUI.LOGGER.throwing(new NullPointerException("Tried adding null widget to " + getClass().getSimpleName()));
            return this;
        }
        if (widget == this) {
            ModularUI.LOGGER.error("Can't add self!");
            return this;
        }
        if (isInitialised()) {
            ModularUI.LOGGER.error("Can't add child after initialised!");
        } else {
            children.add(widget);
        }
        return this;
    }

    @Override
    public List<Widget> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        if (!getChildren().isEmpty()) {
            return getSizeOf(getChildren());
        }
        return new Size(maxWidth, maxHeight);
    }

    public static Size getSizeOf(List<Widget> widgets) {
        int x1 = Integer.MIN_VALUE, y1 = Integer.MIN_VALUE;
        for (Widget widget : widgets) {
            x1 = Math.max(x1, widget.getPos().x + widget.getSize().width);
            y1 = Math.max(y1, widget.getPos().y + widget.getSize().height);
        }
        return new Size(x1, y1);
    }
}
