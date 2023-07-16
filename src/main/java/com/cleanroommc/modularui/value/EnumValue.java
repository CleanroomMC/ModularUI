package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IEnumValue;
import com.cleanroommc.modularui.api.value.IIntValue;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumValue<T extends Enum<T>> implements IEnumValue<T>, IIntValue<T> {

    private final Class<T> enumClass;
    private T value;

    public EnumValue(Class<T> enumClass, T value) {
        this.enumClass = enumClass;
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value, boolean setSource) {
        this.value = value;
    }

    @Override
    public int getIntValue() {
        return value.ordinal();
    }

    @Override
    public void setIntValue(int val, boolean setSource) {
        setValue(this.enumClass.getEnumConstants()[0], setSource);
    }

    @Override
    public Class<T> getEnumClass() {
        return enumClass;
    }

    public static class Dynamic<T extends Enum<T>> implements IEnumValue<T>, IIntValue<T> {

        private final Class<T> enumClass;
        private final Supplier<T> getter;
        private final Consumer<T> setter;

        public Dynamic(Class<T> enumClass, Supplier<T> getter, Consumer<T> setter) {
            this.enumClass = enumClass;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public int getIntValue() {
            return getValue().ordinal();
        }

        @Override
        public void setIntValue(int val, boolean setSource) {
            setValue(this.enumClass.getEnumConstants()[val], setSource);
        }

        @Override
        public T getValue() {
            return this.getter.get();
        }

        @Override
        public void setValue(T value, boolean setSource) {
            this.setter.accept(value);
        }

        @Override
        public Class<T> getEnumClass() {
            return enumClass;
        }
    }
}
