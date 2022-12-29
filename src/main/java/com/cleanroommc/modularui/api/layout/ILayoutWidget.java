package com.cleanroommc.modularui.api.layout;

public interface ILayoutWidget {

    void layoutWidgets();

    default void postLayoutWidgets() {
    }
}
