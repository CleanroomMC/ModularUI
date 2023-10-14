package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.widget.sizer.Area;

/**
 * An interface that handles resizing of widgets.
 * Usually this interface is not implemented by the users of this library or will even interact with it.
 */
public interface IResizeable {

    /**
     * Called once before resizing
     */
    void initResizing();

    /**
     * Resizes the given element
     *
     * @param guiElement element to resize
     * @return true if element is fully resized
     */
    boolean resize(IGuiElement guiElement);

    /**
     * Called if {@link #resize(IGuiElement)} returned false after children have been resized.
     *
     * @param guiElement element to resize
     * @return if element is fully resized
     */
    boolean postResize(IGuiElement guiElement);

    /**
     * Called after all elements in the tree are resized and the absolute positions needs to be calculated from the
     * relative postion.
     *
     * @param guiElement element that was resized
     */
    default void applyPos(IGuiElement guiElement) {
    }

    /**
     * @return area of the element
     */
    // TODO doesnt fit with the other api methods in this interface
    Area getArea();

    /**
     * @return true if the relative x position is calculated
     */
    boolean isXCalculated();

    /**
     * @return true if the relative y position is calculated
     */
    boolean isYCalculated();

    /**
     * @return true if the width is calculated
     */
    boolean isWidthCalculated();

    /**
     * @return true if the height is calculated
     */
    boolean isHeightCalculated();

    default boolean isSizeCalculated(GuiAxis axis) {
        return axis.isHorizontal() ? isWidthCalculated() : isHeightCalculated();
    }

    default boolean isPosCalculated(GuiAxis axis) {
        return axis.isHorizontal() ? isXCalculated() : isYCalculated();
    }

    /**
     * @return true if the relative position and size are fully calculated
     */
    default boolean isFullyCalculated() {
        return isXCalculated() && isYCalculated() && isWidthCalculated() && isHeightCalculated();
    }

    /**
     * Marks position and size as calculated.
     */
    void setResized(boolean x, boolean y, boolean w, boolean h);

    default void setPosResized(boolean x, boolean y) {
        setResized(x, y, isWidthCalculated(), isHeightCalculated());
    }

    default void setSizeResized(boolean w, boolean h) {
        setResized(isXCalculated(), isYCalculated(), w, h);
    }

    default void setXResized(boolean v) {
        setResized(v, isYCalculated(), isWidthCalculated(), isHeightCalculated());
    }

    default void setYResized(boolean v) {
        setResized(isXCalculated(), v, isWidthCalculated(), isHeightCalculated());
    }

    default void setWidthResized(boolean v) {
        setResized(isXCalculated(), isYCalculated(), v, isHeightCalculated());
    }

    default void setHeightResized(boolean v) {
        setResized(isXCalculated(), isYCalculated(), isWidthCalculated(), v);
    }

    default void setResized(boolean b) {
        setResized(b, b, b, b);
    }

    /**
     * Sets if margin and padding on the x-axis is applied
     * @param b true if margin and padding are applied
     */
    void setXMarginPaddingApplied(boolean b);

    /**
     * Sets if margin and padding on the y-axis is applied
     * @param b true if margin and padding are applied
     */
    void setYMarginPaddingApplied(boolean b);

    default void setMarginPaddingApplied(boolean b) {
        setXMarginPaddingApplied(b);
        setYMarginPaddingApplied(b);
    }

    /**
     * @return true if margin and padding are applied on the x-axis
     */
    boolean isXMarginPaddingApplied();

    /**
     * @return true if margin and padding are applied on the y-axis
     */
    boolean isYMarginPaddingApplied();
}
