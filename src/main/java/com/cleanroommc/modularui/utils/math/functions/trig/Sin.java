package com.cleanroommc.modularui.utils.math.functions.trig;

import com.cleanroommc.modularui.api.IValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class Sin extends NNFunction {

    public Sin(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double doubleValue() {
        return Math.sin(this.getArg(0).doubleValue());
    }
}