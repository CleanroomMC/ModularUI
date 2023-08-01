package com.cleanroommc.modularui.utils.math;

import com.cleanroommc.modularui.api.IMathValue;

/**
 * Ternary operator class
 * <p>
 * This value implementation allows to return different values depending on
 * given condition value
 */
public class Ternary implements IMathValue {

    public IMathValue condition;
    public IMathValue ifTrue;
    public IMathValue ifFalse;

    private final IMathValue result = new Constant(0);

    public Ternary(IMathValue condition, IMathValue ifTrue, IMathValue ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public IMathValue get() {
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