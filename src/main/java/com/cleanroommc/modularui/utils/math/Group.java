package com.cleanroommc.modularui.utils.math;

import com.cleanroommc.modularui.api.IMathValue;

/**
 * Group class
 * <p>
 * Simply wraps given {@link IMathValue} into parenthesis in the
 * {@link #toString()} method.
 */
public class Group implements IMathValue {

    private final IMathValue value;

    public Group(IMathValue value) {
        this.value = value;
    }

    @Override
    public IMathValue get() {
        return this.value.get();
    }

    @Override
    public boolean isNumber() {
        return this.value.isNumber();
    }

    @Override
    public void set(double value) {
        this.value.set(value);
    }

    @Override
    public void set(String value) {
        this.value.set(value);
    }

    @Override
    public double doubleValue() {
        return this.value.doubleValue();
    }

    @Override
    public boolean booleanValue() {
        return this.value.booleanValue();
    }

    @Override
    public String stringValue() {
        return this.value.stringValue();
    }

    @Override
    public String toString() {
        return "(" + this.value.toString() + ")";
    }
}