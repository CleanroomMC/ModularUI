package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.math.Size;

import javax.annotation.Nullable;
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

    @Nullable
    @Override
    protected Size determineSize() {
        if (!getChildren().isEmpty()) {
            float x0 = Float.MAX_VALUE, x1 = 0, y0 = Float.MAX_VALUE, y1 = 0;
            for (Widget widget : getChildren()) {
                x0 = Math.min(x0, widget.getPos().x);
                x1 = Math.max(x1, widget.getPos().x + widget.getSize().width);
                y0 = Math.min(y0, widget.getPos().y);
                y1 = Math.max(y1, widget.getPos().y + widget.getSize().height);
            }
            return new Size(x1 - x0, y1 - y0);
        }
        return null;
    }
}