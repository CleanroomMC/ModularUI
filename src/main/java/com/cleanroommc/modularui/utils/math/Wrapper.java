package com.cleanroommc.modularui.utils.math;

import com.cleanroommc.modularui.api.IValue;

public abstract class Wrapper implements IValue {

    public IValue value;

    protected IValue result = new Constant(0);

    public Wrapper(IValue value) {
        this.value = value;
    }

    @Override
    public IValue get() {
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
