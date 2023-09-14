package com.cleanroommc.modularui.api.layout;

/**
 * This is responsible for laying out widgets.
 */
public interface ILayoutWidget {

    /**
     * Called after the children tried to calculate their size.
     * Might be called multiple times.
     */
    void layoutWidgets();

    /**
     * Called after post calculation of this widget.
     * Might be called multiple times.
     * The last call guarantees, that this widget is fully calculated.
     */
    default void postLayoutWidgets() {
    }
}
