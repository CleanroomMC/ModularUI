package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.screen.RichTooltip;

import org.jetbrains.annotations.Nullable;

public interface IHoverable extends IIcon {

    /**
     * Called every frame this hoverable is hovered inside a {@link com.cleanroommc.modularui.drawable.text.RichText}.
     */
    default void onHover() {}

    @Nullable
    default RichTooltip getTooltip() {
        return null;
    }
}
