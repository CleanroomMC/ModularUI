package com.cleanroommc.modularui.api.widget;

/**
 * Marks a widget to contain a value
 *
 * @param <T>
 */
public interface IValueWidget<T> {

    /**
     * @return stored value
     */
    T getWidgetValue();
}
