package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.GuiContext;

public interface IFocusedWidget {

    boolean isFocused();

    void onFocus(GuiContext context);

    void onUnfocus(GuiContext context);

    void selectAll(GuiContext context);

    void unselect(GuiContext context);
}
