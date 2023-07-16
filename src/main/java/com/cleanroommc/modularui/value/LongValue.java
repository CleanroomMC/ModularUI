package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.api.value.ILongValue;
import com.cleanroommc.modularui.api.value.IStringValue;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

public class LongValue implements ILongValue<Long>, IIntValue<Long>, IStringValue<Long> {

    private long value;

    public LongValue(long value) {
        this.value = value;
    }

    @Override
    public Long getValue() {
        return getLongValue();
    }

    @Override
    public void setValue(Long value, boolean setSource) {
        setLongValue(value, setSource);
    }

    @Override
    public long getLongValue() {
        return value;
    }

    @Override
    public void setLongValue(long val, boolean setSource) {
        this.value = val;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.value);
    }

    @Override
    public void setStringValue(String val, boolean setSource) {
        setLongValue(Long.parseLong(val), setSource);
    }

    @Override
    public int getIntValue() {
        return (int) this.value;
    }

    @Override
    public void setIntValue(int val, boolean setSource) {
        setLongValue(val, setSource);
    }

    public static class Dynamic implements ILongValue<Long>, IIntValue<Long>, IStringValue<Long> {

        private final LongSupplier getter;
        private final LongConsumer setter;

        public Dynamic(LongSupplier getter, LongConsumer setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public long getLongValue() {
            return this.getter.getAsLong();
        }

        @Override
        public void setLongValue(long val, boolean setSource) {
            this.setter.accept(val);
        }

        @Override
        public String getStringValue() {
            return String.valueOf(getLongValue());
        }

        @Override
        public void setStringValue(String val, boolean setSource) {
            setLongValue(Long.parseLong(val), setSource);
        }

        @Override
        public Long getValue() {
            return getLongValue();
        }

        @Override
        public void setValue(Long value, boolean setSource) {
            setLongValue(value, setSource);
        }

        @Override
        public int getIntValue() {
            return (int) getLongValue();
        }

        @Override
        public void setIntValue(int val, boolean setSource) {
            setLongValue(val, setSource);
        }
    }
}
