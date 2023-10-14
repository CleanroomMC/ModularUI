package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.screen.viewport.GuiContext;

/**
 * An interface for {@link IWidget}'s, that makes them focusable.
 */
public interface IFocusedWidget {

    /**
     * @return this widget is currently focused
     */
    boolean isFocused();

    /**
     * Called when this widget gets focused
     *
     * @param context gui context
     */
    void onFocus(GuiContext context);

    /**
     * Called when the focus is removed from this widget
     *
     * @param context gui context
     */
    void onRemoveFocus(GuiContext context);
}
