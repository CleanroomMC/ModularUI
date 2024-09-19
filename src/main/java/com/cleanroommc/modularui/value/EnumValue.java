package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IEnumValue;
import com.cleanroommc.modularui.api.value.IIntValue;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumValue<T extends Enum<T>> implements IEnumValue<T>, IIntValue<T> {

    protected final Class<T> enumClass;
    protected T value;

    public EnumValue(Class<T> enumClass, T value) {
        this.enumClass = enumClass;
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

    @Override
    public int getIntValue() {
        return this.value.ordinal();
    }

    @Override
    public void setIntValue(int val) {
        setValue(this.enumClass.getEnumConstants()[val]);
    }

    @Override
    public Class<T> getEnumClass() {
        return this.enumClass;
    }

    public static class Dynamic<T extends Enum<T>> implements IEnumValue<T>, IIntValue<T> {

        protected final Class<T> enumClass;
        protected final Supplier<T> getter;
        protected final Consumer<T> setter;

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
        public void setIntValue(int val) {
            setValue(this.enumClass.getEnumConstants()[val]);
        }

        @Override
        public T getValue() {
            return this.getter.get();
        }

        @Override
        public void setValue(T value) {
            this.setter.accept(value);
        }

        @Override
        public Class<T> getEnumClass() {
            return this.enumClass;
        }
    }
}
