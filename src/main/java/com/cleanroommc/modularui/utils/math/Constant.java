package com.cleanroommc.modularui.utils.math;

import com.cleanroommc.modularui.api.sync.IValue;

/**
 * Constant class
 * <p>
 * This class simply returns supplied in the constructor value
 */
public class Constant implements IValue {

    private double doubleValue;
    private String stringValue;

    public Constant(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public Constant(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public IValue get() {
        return this;
    }

    @Override
    public boolean isNumber() {
        return this.stringValue == null;
    }

    @Override
    public void set(double value) {
        this.doubleValue = value;
        this.stringValue = null;
    }

    @Override
    public void set(String value) {
        this.doubleValue = 0;
        this.stringValue = value;
    }

    @Override
    public double doubleValue() {
        return this.doubleValue;
    }

    @Override
    public boolean booleanValue() {
        if (this.isNumber()) {
            return Operation.isTrue(this.doubleValue);
        }

        return this.stringValue.equalsIgnoreCase("true");
    }

    @Override
    public String stringValue() {
        return this.stringValue;
    }

    @Override
    public String toString() {
        return this.stringValue == null ? String.valueOf(this.doubleValue) : "\"" + this.stringValue + "\"";
    }
}