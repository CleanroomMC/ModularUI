package com.cleanroommc.modularui.api;

public interface ILayoutWidget {

    void layoutWidgets();

    default void postLayoutWidgets() {
    }
}
