package com.cleanroommc.modularui.common.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
import com.cleanroommc.modularui.api.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SingleChildWidget extends Widget implements IWidgetParent {

    private Widget child;

    public SingleChildWidget() {
    }

    public SingleChildWidget(Size size) {
        super(size);
    }

    public SingleChildWidget(Size size, Pos2d pos) {
        super(size, pos);
    }

    public final SingleChildWidget setChild(Widget widget) {
        if (this.child != null) {
            ModularUI.LOGGER.error("Child is already set!");
        } else if (MultiChildWidget.checkEditable(this)) {
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

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        return child.getSize();
    }
}
