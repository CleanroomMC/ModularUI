package com.cleanroommc.modularui.widgets.menu;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class MenuPanel extends ModularPanel {

    public MenuPanel(ContextMenuList<?> menuList) {
        super(menuList.getName());
        fullScreenInvisible();
        child(menuList);
        themeOverride("modularui.context_menu");
    }

    public void openSubMenu(ContextMenuList<?> menuList) {
        child(menuList);
    }

    @Override
    protected void onChildAdd(IWidget child) {
        super.onChildAdd(child);
        child.scheduleResize();
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    public boolean closeOnOutOfBoundsClick() {
        return true;
    }
}
