package com.cleanroommc.modularui.utils.math.functions.classic;

import com.cleanroommc.modularui.api.IValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class Mod extends NNFunction {

    public Mod(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    public double doubleValue() {
        return this.getArg(0).doubleValue() % this.getArg(1).doubleValue();
    }
}