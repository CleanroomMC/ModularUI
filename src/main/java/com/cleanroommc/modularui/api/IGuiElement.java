package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widget.sizer.Area;
import org.jetbrains.annotations.ApiStatus;

public interface IGuiElement {

    ModularScreen getScreen();

    IGuiElement getParent();

    Area getArea();

    default Area getParentArea() {
        return getParent().getArea();
    }

    void draw(float partialTicks);

    @ApiStatus.OverrideOnly
    default void onMouseStartHover() {
    }

    @ApiStatus.OverrideOnly
    default void onMouseEndHover() {
    }

    default boolean isHovering() {
        return getScreen().context.isHovered(this);
    }

    boolean isEnabled();

    void resize();

    default int getDefaultWidth() {
        return 20;
    }

    default int getDefaultHeight() {
        return 20;
    }
}
