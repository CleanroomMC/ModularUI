package com.cleanroommc.modularui.api;

@FunctionalInterface
public interface IWidgetDrawable {

    /**
     * Is called before any other draw calls in gui
     *
     * @param partialTicks ticks since last draw
     */
    void drawInBackground(float partialTicks);

    /**
     * Is called after most gui draw calls
     *
     * @param partialTicks ticks since last draw
     */
    default void drawInForeground(float partialTicks) {
    }
}
