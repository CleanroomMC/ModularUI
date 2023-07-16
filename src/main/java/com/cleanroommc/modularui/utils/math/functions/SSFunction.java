package com.cleanroommc.modularui.utils.math.functions;

import com.cleanroommc.modularui.api.IMathValue;

/**
 * Function that expects string input arguments and outputs a string
 */
public abstract class SSFunction extends Function {

    public SSFunction(IMathValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected void verifyArgument(int index, IMathValue value) {
        if (value.isNumber()) {
            throw new IllegalStateException("Function " + this.name + " cannot receive number arguments!");
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
