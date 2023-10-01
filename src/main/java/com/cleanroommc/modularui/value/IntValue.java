package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.api.value.IStringValue;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntValue implements IIntValue<Integer>, IStringValue<Integer> {

    private int value;

    public IntValue(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return getIntValue();
    }

    @Override
    public void setValue(Integer value) {
        setIntValue(value);
    }

    @Override
    public int getIntValue() {
        return this.value;
    }

    @Override
    public void setIntValue(int val) {
        this.value = val;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.value);
    }

    @Override
    public void setStringValue(String val) {
        setIntValue(Integer.parseInt(val));
    }

    public static class Dynamic implements IIntValue<Integer>, IStringValue<Integer> {

        private final IntSupplier getter;
        private final IntConsumer setter;

        public Dynamic(IntSupplier getter, IntConsumer setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public int getIntValue() {
            return this.getter.getAsInt();
        }

        @Override
        public void setIntValue(int val) {
            this.setter.accept(val);
        }

        @Override
        public String getStringValue() {
            return String.valueOf(getIntValue());
        }

        @Override
        public void setStringValue(String val) {
            setIntValue(Integer.parseInt(val));
        }

        @Override
        public Integer getValue() {
            return getIntValue();
        }

        @Override
        public void setValue(Integer value) {
            setIntValue(value);
        }
    }
}
