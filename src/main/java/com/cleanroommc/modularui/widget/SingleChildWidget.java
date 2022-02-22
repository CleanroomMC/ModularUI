package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.ModularUIMod;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;

import java.util.Collections;
import java.util.List;

public abstract class SingleChildWidget extends Widget implements IWidgetParent {

    private Widget child;

    public SingleChildWidget() {
    }

    public SingleChildWidget(Size size) {
        super(size);
    }

    public SingleChildWidget(Size size, Pos2d pos) {
        super(size, pos);
    }

    public SingleChildWidget(Size size, Alignment alignment) {
        super(size, alignment);
    }


    public final SingleChildWidget setChild(Widget widget) {
        if (isInitialised()) {
            ModularUIMod.LOGGER.error("Can't add child after initialised!");
        } else if (this.child != null) {
            ModularUIMod.LOGGER.error("Child is already set!");
        } else {
            this.child = widget;
        }
        return this;
    }

    @Override
    public List<Widget> getChildren() {
        return Collections.singletonList(child);
    }

    public Widget getChild() {
        return child;
    }
}
