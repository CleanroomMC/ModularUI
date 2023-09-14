package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.api.widget.IGuiElement;

public interface IResizeable {

    void initResizing();

    boolean resize(IGuiElement guiElement);

    boolean postResize(IGuiElement guiElement);

    default void applyPos(IGuiElement guiElement) {
    }

    Area getArea();

    boolean isXCalculated();

    boolean isYCalculated();

    boolean isWidthCalculated();

    boolean isHeightCalculated();

    default boolean isSizeCalculated(GuiAxis axis) {
        return axis.isHorizontal() ? isWidthCalculated() : isHeightCalculated();
    }

    default boolean isPosCalculated(GuiAxis axis) {
        return axis.isHorizontal() ? isXCalculated() : isYCalculated();
    }

    default boolean isFullyCalculated() {
        return isXCalculated() && isYCalculated() && isWidthCalculated() && isHeightCalculated();
    }

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
}