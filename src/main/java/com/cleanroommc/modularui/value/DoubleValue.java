package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IDoubleValue;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.utils.FloatConsumer;
import com.cleanroommc.modularui.utils.FloatSupplier;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DoubleValue implements IDoubleValue<Double> {

    public static Dynamic wrap(IDoubleValue<?> val) {
        return new Dynamic(val::getDoubleValue, val::setDoubleValue);
    }

    public static Dynamic wrapAtomic(AtomicDouble val) {
        return new Dynamic(val::get, val::set);
    }

    private double value;

    public DoubleValue(double value) {
        this.value = value;
    }

    @Override
    public Double getValue() {
        return getDoubleValue();
    }

    @Override
    public void setValue(Double value) {
        setDoubleValue(value);
    }

    @Override
    public double getDoubleValue() {
        return this.value;
    }

    @Override
    public void setDoubleValue(double val) {
        this.value = val;
    }

    public static class Dynamic implements IDoubleValue<Double>, IStringValue<Double> {

        public static Dynamic ofFloat(FloatSupplier getter, FloatConsumer setter) {
            return new Dynamic(getter, setter);
        }

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
        public void setDoubleValue(double val) {
            this.setter.accept(val);
        }

        @Override
        public String getStringValue() {
            return String.valueOf(getDoubleValue());
        }

        @Override
        public void setStringValue(String val) {
            setDoubleValue(Double.parseDouble(val));
        }

        @Override
        public Double getValue() {
            return getDoubleValue();
        }

        @Override
        public void setValue(Double value) {
            setDoubleValue(value);
        }
    }
}
