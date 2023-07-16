package com.cleanroommc.modularui.api.value;

public interface IIntValue<T> extends IValue<T> {

    int getIntValue();

    default void setIntValue(int val) {
        setIntValue(val, true);
    }

    void setIntValue(int val, boolean setSource);
}
