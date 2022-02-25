package com.cleanroommc.modularui.common.widget;

@FunctionalInterface
public interface IWidgetDrawable {

    void drawInBackground(float partialTicks);

    default void drawInForeground(float partialTicks) {

    }
}
