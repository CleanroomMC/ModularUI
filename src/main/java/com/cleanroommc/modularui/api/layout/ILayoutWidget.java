package com.cleanroommc.modularui.api.layout;

/**
 * This is responsible for laying out widgets.
 */
public interface ILayoutWidget {

    /**
     * Called when this should re-layout it's children.
     */
    void layoutWidgets();

    /**
     * Can be used for some extra calculations
     * TODO: Really needed?
     */
    default void postLayoutWidgets() {
    }
}
