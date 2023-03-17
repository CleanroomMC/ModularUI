package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.screen.viewport.GuiContext;

/**
 * An interface for {@link IWidget} objects, that makes them focusable.
 */
public interface IFocusedWidget {

    boolean isFocused();

    void onFocus(GuiContext context);

    void onRemoveFocus(GuiContext context);

    @Deprecated
    void selectAll(GuiContext context);

    @Deprecated
    void unselect(GuiContext context);
}
