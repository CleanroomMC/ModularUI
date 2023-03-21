package com.cleanroommc.modularui.utils.math;

import com.cleanroommc.modularui.api.IValue;

/**
 * Operator class
 * <p>
 * This class is responsible for performing a calculation of two values
 * based on given operation.
 */
public class Operator implements IValue {

    public static boolean DEBUG = false;

    public Operation operation;
    public IValue a;
    public IValue b;
    private final IValue result = new Constant(0);

    public Operator(Operation op, IValue a, IValue b) {
        this.operation = op;
        this.a = a;
        this.b = b;
    }

    @Override
    public IValue get() {
        if (!this.isNumber() && this.operation == Operation.ADD) {
            this.result.set(this.stringValue());
        } else {
            this.result.set(this.doubleValue());
        }

        return this.result;
    }

    @Override
    public boolean isNumber() {
        return this.a.isNumber() || this.b.isNumber();
    }

    @Override
    public void set(double value) {
    }

    @Override
    public void set(String value) {
    }

    @Override
    public double doubleValue() {
        if (!this.isNumber() && this.operation == Operation.EQUALS) {
            return this.a.stringValue().equals(this.b.stringValue()) ? 1 : 0;
        }

        return this.operation.calculate(this.a.doubleValue(), this.b.doubleValue());
    }

    @Override
    public boolean booleanValue() {
        return Operation.isTrue(this.doubleValue());
    }

    @Override
    public String stringValue() {
        if (this.operation == Operation.ADD) {
            return this.a.stringValue() + this.b.stringValue();
        }

        return this.a.stringValue();
    }

    @Override
    public String toString() {
        if (DEBUG) {
            return "(" + this.a.toString() + " " + this.operation.sign + " " + this.b.toString() + ")";
        }

        return this.a.toString() + " " + this.operation.sign + " " + this.b.toString();
    }
}