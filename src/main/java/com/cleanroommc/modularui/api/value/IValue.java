package com.cleanroommc.modularui.api.value;

public interface IValue<T> {

    T getValue();

    default void setValue(T value) {
        setValue(value, true);
    }

    void setValue(T value, boolean setSource);
}
