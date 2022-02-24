package com.cleanroommc.modularui.widget;

@FunctionalInterface
public interface IWidgetDrawable {

    void drawInBackground(float partialTicks);

    default void drawInForeground(float partialTicks) {

    }
}
