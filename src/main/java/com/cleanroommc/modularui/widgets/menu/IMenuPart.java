package com.cleanroommc.modularui.widgets.menu;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.WidgetTree;

public interface IMenuPart extends IWidget {

    default boolean isSelfOrChildHovered() {
        return isBelowMouse() || !WidgetTree.foreachChild(this,
                w -> !(w instanceof IMenuPart menuPart ? menuPart.isSelfOrChildHovered() : w.isBelowMouse()),
                false);
    }
}
