package com.cleanroommc.modularui.utils.math.functions;

import com.cleanroommc.modularui.api.IMathValue;

/**
 * Function that expects number input arguments and outputs a string
 */
public abstract class NSFunction extends Function {

    public NSFunction(IMathValue[] values, String name) throws Exception {
        super(values, name);

        for (IMathValue value : values) {
            if (!value.isNumber()) {
                throw new IllegalStateException("Function " + name + " cannot receive string arguments!");
            }
        }
    }

    @Override
    protected void verifyArgument(int index, IMathValue value) {
        if (!value.isNumber()) {
            throw new IllegalStateException("Function " + this.name + " cannot receive string arguments!");
        }
    }

    @Override
    public IMathValue get() {
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
