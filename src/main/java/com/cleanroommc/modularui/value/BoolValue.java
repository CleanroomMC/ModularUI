package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IBoolValue;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.utils.BooleanConsumer;

import java.util.function.BooleanSupplier;

public class BoolValue implements IBoolValue<Boolean>, IStringValue<Boolean> {

    private boolean value;

    public BoolValue(boolean value) {
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return getBoolValue();
    }

    @Override
    public void setValue(Boolean value) {
        setBoolValue(value);
    }

    @Override
    public boolean getBoolValue() {
        return this.value;
    }

    @Override
    public void setBoolValue(boolean val) {
        this.value = val;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.value);
    }

    @Override
    public void setStringValue(String val) {
        setBoolValue(Boolean.parseBoolean(val));
    }

    public static class Dynamic implements IBoolValue<Boolean>, IIntValue<Boolean>, IStringValue<Boolean> {

        private final BooleanSupplier getter;
        private final BooleanConsumer setter;

        public Dynamic(BooleanSupplier getter, BooleanConsumer setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public boolean getBoolValue() {
            return this.getter.getAsBoolean();
        }

        @Override
        public void setBoolValue(boolean val) {
            this.setter.accept(val);
        }

        @Override
        public String getStringValue() {
            return String.valueOf(getBoolValue());
        }

        @Override
        public void setStringValue(String val) {
            setBoolValue(Boolean.parseBoolean(val));
        }

        @Override
        public Boolean getValue() {
            return getBoolValue();
        }

        @Override
        public void setValue(Boolean value) {
            setBoolValue(value);
        }

        @Override
        public int getIntValue() {
            return getBoolValue() ? 1 : 0;
        }

        @Override
        public void setIntValue(int val) {
            setBoolValue(val == 1);
        }
    }
}
