package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IValue;

import org.jetbrains.annotations.ApiStatus;

/**
 * @deprecated use {@link ObjectValue} instead
 */
@ApiStatus.ScheduledForRemoval(inVersion = "3.2.0")
@Deprecated
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
    public void setValue(T value) {
        this.value = value;
    }
}
