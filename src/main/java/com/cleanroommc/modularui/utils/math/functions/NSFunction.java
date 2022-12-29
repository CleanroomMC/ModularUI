package com.cleanroommc.modularui.utils.math.functions;

import com.cleanroommc.modularui.api.sync.IValue;

/**
 * Function that expects number input arguments and outputs a string
 */
public abstract class NSFunction extends Function {

    public NSFunction(IValue[] values, String name) throws Exception {
        super(values, name);

        for (IValue value : values) {
            if (!value.isNumber()) {
                throw new IllegalStateException("Function " + name + " cannot receive string arguments!");
            }
        }
    }

    @Override
    protected void verifyArgument(int index, IValue value) {
        if (!value.isNumber()) {
            throw new IllegalStateException("Function " + this.name + " cannot receive string arguments!");
        }
    }

    @Override
    public IValue get() {
        this.result.set(this.stringValue());

        return this.result;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public double doubleValue() {
        return 0;
    }

    @Override
    public boolean booleanValue() {
        return this.stringValue().equalsIgnoreCase("true");
    }
}
