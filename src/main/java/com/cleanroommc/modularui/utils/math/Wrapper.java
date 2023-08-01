package com.cleanroommc.modularui.utils.math;

import com.cleanroommc.modularui.api.IMathValue;

public abstract class Wrapper implements IMathValue {

    public IMathValue value;

    protected IMathValue result = new Constant(0);

    public Wrapper(IMathValue value) {
        this.value = value;
    }

    @Override
    public IMathValue get() {
        this.process();

        return this.result;
    }

    protected abstract void process();

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
}
