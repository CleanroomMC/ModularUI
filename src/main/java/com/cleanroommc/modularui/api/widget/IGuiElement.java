package com.cleanroommc.modularui.api.widget;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.layout.IResizeable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.sizer.Area;

/**
 * Base interface for gui elements. For example widgets.
 */
public interface IGuiElement {

    /**
     * @return the screen this element is in
     */
    ModularScreen getScreen();

    /**
     * @return the parent of this element
     */
    IGuiElement getParent();

    IResizeable resizer();

    /**
     * @return the area this element occupies
     */
    Area getArea();

    /**
     * Shortcut to get the area of the parent
     *
     * @return parent area
     */
    default Area getParentArea() {
        return getParent().getArea();
    }

    /**
     * Applies default backgrounds and colors if they haven't been set already.
     *
     * @param theme themes to apply
     */
    default void applyTheme(ITheme theme) {
    }


    /**
     * Draws this element
     *
     * @param context gui context
     */
    void draw(GuiContext context);

    /**
     * Called when the mouse enters the area of this element
     */
    default void onMouseStartHover() {
    }

    /**
     * Called when the mouse leaves the area of this element
     */
    default void onMouseEndHover() {
    }

    /**
     * @return if this widget is currently right below the mouse
     */
    default boolean isHovering() {
        return getScreen().context.isHovered(this);
    }

    /**
     * Returns if this element is right blow the mouse for a certain amount of time
     *
     * @param ticks time in ticks
     * @return if this element is right blow the mouse for a certain amount of time
     */
    default boolean isHoveringFor(int ticks) {
        return getScreen().context.isHoveredFor(this, ticks);
    }

    default boolean isBelowMouse() {
        IGuiElement hovered = getScreen().context.getHovered();
        if (hovered == null) return false;
        while (!(hovered instanceof ModularPanel)) {
            if (hovered == this) return true;
            hovered = hovered.getParent();
        }
        return hovered == this;
    }

    /**
     * Returns if this element is enabled. Disabled elements are not drawn and can not be interacted with.
     *
     * @return if this element is enabled
     */
    boolean isEnabled();

    /**
     * @return default width if it can't be calculated
     */
    default int getDefaultWidth() {
        return 18;
    }

    /**
     * @return default height if it can't be calculated
     */
    default int getDefaultHeight() {
        return 18;
    }
}
