package com.cleanroommc.modularui.api.value;

public interface IValue<T> {

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    T getValue();

    /**
     * Updates the current value.
     *
     * @param value new value
     */
    default void setValue(T value) {
        setValue(value, true);
    }

    void setValue(T value, boolean setSource);
}
