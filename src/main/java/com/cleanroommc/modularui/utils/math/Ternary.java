package com.cleanroommc.modularui.utils.math;

import com.cleanroommc.modularui.api.IValue;

/**
 * Ternary operator class
 * <p>
 * This value implementation allows to return different values depending on
 * given condition value
 */
public class Ternary implements IValue {

    public IValue condition;
    public IValue ifTrue;
    public IValue ifFalse;

    private final IValue result = new Constant(0);

    public Ternary(IValue condition, IValue ifTrue, IValue ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public IValue get() {
        if (this.isNumber()) {
            this.result.set(this.doubleValue());
        } else {
            this.result.set(this.stringValue());
        }

        return this.result;
    }

    @Override
    public boolean isNumber() {
        return this.ifFalse.isNumber() || this.ifTrue.isNumber();
    }

    @Override
    public void set(double value) {
    }

    @Override
    public void set(String value) {
    }

    @Override
    public double doubleValue() {
        return Operation.isTrue(this.condition.doubleValue()) ? this.ifTrue.doubleValue() : this.ifFalse.doubleValue();
    }

    @Override
    public boolean booleanValue() {
        return Operation.isTrue(this.doubleValue());
    }

    @Override
    public String stringValue() {
        return Operation.isTrue(this.condition.doubleValue()) ? this.ifTrue.stringValue() : this.ifFalse.stringValue();
    }

    @Override
    public String toString() {
        return this.condition.toString() + " ? " + this.ifTrue.toString() + " : " + this.ifFalse.toString();
    }
}