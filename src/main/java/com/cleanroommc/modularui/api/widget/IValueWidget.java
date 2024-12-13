package com.cleanroommc.modularui.api.widget;

/**
 * Marks a widget to contain a value
 *
 * @param <T>
 */
public interface IValueWidget<T> extends IWidget {

    /**
     * @return stored value
     */
    T getWidgetValue();
}
