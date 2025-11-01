package com.cleanroommc.modularui.widgets.menu;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.widget.WidgetTree;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

@ApiStatus.Experimental
public interface IContextMenuOption extends IWidget {

    default void closeParent() {
        ContextMenuList<?> menuList = WidgetTree.findParent(this, ContextMenuList.class);
        Objects.requireNonNull(menuList);
        menuList.close();
    }

    default boolean isSelfOrChildHovered() {
        return isBelowMouse();
    }
}
