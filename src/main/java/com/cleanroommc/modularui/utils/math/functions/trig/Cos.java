package com.cleanroommc.modularui.utils.math.functions.trig;

import com.cleanroommc.modularui.api.sync.IValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class Cos extends NNFunction {

    public Cos(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double doubleValue() {
        return Math.cos(this.getArg(0).doubleValue());
    }
}