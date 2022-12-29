package com.cleanroommc.modularui.widget.sizer;

import com.cleanroommc.modularui.api.widget.IGuiElement;

public interface IResizeable {

    void apply(IGuiElement guiElement);

    void postApply(IGuiElement guiElement);

    default void applyPos(IGuiElement guiElement) {
    }

    default boolean isSkip() {
        return false;
    }
}
