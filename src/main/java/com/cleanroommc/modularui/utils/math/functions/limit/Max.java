package com.cleanroommc.modularui.utils.math.functions.limit;

import com.cleanroommc.modularui.api.IMathValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class Max extends NNFunction {

    public Max(IMathValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    public double doubleValue() {
        return Math.max(this.getArg(0).doubleValue(), this.getArg(1).doubleValue());
    }
}