package com.cleanroommc.modularui.utils.math.functions.limit;

import com.cleanroommc.modularui.api.IMathValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class Min extends NNFunction {

    public Min(IMathValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    public double doubleValue() {
        return Math.min(this.getArg(0).doubleValue(), this.getArg(1).doubleValue());
    }
}