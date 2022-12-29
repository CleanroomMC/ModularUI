package com.cleanroommc.modularui.utils.math;

import com.cleanroommc.modularui.api.sync.IValue;

/**
 * Negative operator class
 * <p>
 * This class is responsible for inverting given value
 */
public class Negative extends Wrapper {

    public Negative(IValue value) {
        super(value);
    }

    @Override
    protected void process() {
        this.result.set(this.doubleValue());
    }

    @Override
    public double doubleValue() {
        return -this.value.doubleValue();
    }

    @Override
    public boolean booleanValue() {
        return Operation.isTrue(this.doubleValue());
    }

    @Override
    public String toString() {
        return "-" + this.value.toString();
    }
}