package com.cleanroommc.modularui.utils.math.functions.classic;

import com.cleanroommc.modularui.api.IValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class Exp extends NNFunction {

    public Exp(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double doubleValue() {
        return Math.exp(this.getArg(0).doubleValue());
    }
}