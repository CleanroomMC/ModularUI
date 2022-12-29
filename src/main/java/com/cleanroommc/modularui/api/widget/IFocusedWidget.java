package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.screen.GuiContext;

public interface IFocusedWidget {

    boolean isFocused();

    void onFocus(GuiContext context);

    void onRemoveFocus(GuiContext context);

    void selectAll(GuiContext context);

    void unselect(GuiContext context);
}
