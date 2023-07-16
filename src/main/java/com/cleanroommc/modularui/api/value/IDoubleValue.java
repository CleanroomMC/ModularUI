package com.cleanroommc.modularui.api.value;

public interface IDoubleValue<T> extends IValue<T> {

    double getDoubleValue();

    default void setDoubleValue(double val) {
        setDoubleValue(val, true);
    }

    void setDoubleValue(double val, boolean setSource);
}
