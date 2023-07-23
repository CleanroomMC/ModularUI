package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IValue;

public class ConstValue<T> implements IValue<T> {

    protected T value;

    public ConstValue(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return this.value;
    }

    @Override
    public void setValue(T value, boolean setSource) {
        this.value = value;
    }
}
