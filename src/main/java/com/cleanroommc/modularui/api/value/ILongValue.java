package com.cleanroommc.modularui.api.value;

public interface ILongValue<T> extends IValue<T> {

    long getLongValue();

    default void setLongValue(long val) {
        setLongValue(val, true);
    }

    void setLongValue(long val, boolean setSource);
}
