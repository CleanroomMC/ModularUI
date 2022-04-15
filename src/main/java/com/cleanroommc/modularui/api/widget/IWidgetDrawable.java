package com.cleanroommc.modularui.api.widget;

@FunctionalInterface
public interface IWidgetDrawable {

    /**
     * Is called before any other draw calls in gui
     *
     * @param widget       widget to draw
     * @param partialTicks ticks since last draw
     */
    void drawWidgetCustom(Widget widget, float partialTicks);
}
