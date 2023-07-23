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
    public void setValue(Integer value, boolean setSource) {
        setIntValue(value, setSource);
    }

    @Override
    public int getIntValue() {
        return this.value;
    }

    @Override
    public void setIntValue(int val, boolean setSource) {
        this.value = val;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.value);
    }

    @Override
    public void setStringValue(String val, boolean setSource) {
        setIntValue(Integer.parseInt(val), setSource);
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
        public void setIntValue(int val, boolean setSource) {
            this.setter.accept(val);
        }

        @Override
        public String getStringValue() {
            return String.valueOf(getIntValue());
        }

        @Override
        public void setStringValue(String val, boolean setSource) {
            setIntValue(Integer.parseInt(val), setSource);
        }

        @Override
        public Integer getValue() {
            return getIntValue();
        }

        @Override
        public void setValue(Integer value, boolean setSource) {
            setIntValue(value, setSource);
        }
    }
}
