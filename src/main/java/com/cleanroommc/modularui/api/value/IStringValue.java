package com.cleanroommc.modularui.api.value;

public interface IStringValue<T> extends IValue<T> {

    String getStringValue();

    default void setStringValue(String val) {
        setStringValue(val, true);
    }

    void setStringValue(String val, boolean setSource);
}
