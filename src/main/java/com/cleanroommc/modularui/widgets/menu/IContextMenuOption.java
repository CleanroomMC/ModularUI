package com.cleanroommc.modularui.widgets.menu;

import com.cleanroommc.modularui.api.widget.IWidget;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface IContextMenuOption extends IWidget {

    default boolean isSelfOrChildHovered() {
        return isBelowMouse();
    }
}
