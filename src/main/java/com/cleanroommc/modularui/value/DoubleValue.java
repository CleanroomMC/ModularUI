package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IDoubleValue;
import com.cleanroommc.modularui.api.value.IStringValue;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DoubleValue implements IDoubleValue<Double>, IStringValue<Double> {

    private double value;

    public DoubleValue(double value) {
        this.value = value;
    }

    @Override
    public Double getValue() {
        return getDoubleValue();
    }

    @Override
    public void setValue(Double value, boolean setSource) {
        setDoubleValue(value, setSource);
    }

    @Override
    public double getDoubleValue() {
        return value;
    }

    @Override
    public void setDoubleValue(double val, boolean setSource) {
        this.value = val;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.value);
    }

    @Override
    public void setStringValue(String val, boolean setSource) {
        setDoubleValue(Double.parseDouble(val), setSource);
    }

    public static class Dynamic implements IDoubleValue<Double>, IStringValue<Double> {

        private final DoubleSupplier getter;
        private final DoubleConsumer setter;

        public Dynamic(DoubleSupplier getter, DoubleConsumer setter) {
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public double getDoubleValue() {
            return this.getter.getAsDouble();
        }

        @Override
        public void setDoubleValue(double val, boolean setSource) {
            this.setter.accept(val);
        }

        @Override
        public String getStringValue() {
            return String.valueOf(getDoubleValue());
        }

        @Override
        public void setStringValue(String val, boolean setSource) {
            setDoubleValue(Double.parseDouble(val), setSource);
        }

        @Override
        public Double getValue() {
            return getDoubleValue();
        }

        @Override
        public void setValue(Double value, boolean setSource) {
            setDoubleValue(value, setSource);
        }
    }
}
