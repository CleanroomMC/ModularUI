package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IValue;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DynamicValue<T> implements IValue<T> {

    private final Supplier<T> getter;
    @Nullable
    private final Consumer<T> setter;

    public DynamicValue(Supplier<T> getter, @Nullable Consumer<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public T getValue() {
        return this.getter.get();
    }

    @Override
    public void setValue(T value, boolean setSource) {
        if (this.setter != null) {
            this.setter.accept(value);
        }
    }
}
