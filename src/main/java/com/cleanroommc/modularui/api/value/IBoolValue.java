package com.cleanroommc.modularui.api.value;

public interface IBoolValue<T> extends IValue<T> {

    boolean getBoolValue();

    default void setBoolValue(boolean val) {
        setBoolValue(val, true);
    }

    void setBoolValue(boolean val, boolean setSource);
}
