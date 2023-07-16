package com.cleanroommc.modularui.utils.math.functions.rounding;

import com.cleanroommc.modularui.api.IMathValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class Ceil extends NNFunction {

    public Ceil(IMathValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double doubleValue() {
        return Math.ceil(this.getArg(0).doubleValue());
    }
}