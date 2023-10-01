package com.cleanroommc.modularui.api.value;

public interface IStringValue<T> extends IValue<T> {

    String getStringValue();

    void setStringValue(String val);
}
