package com.cleanroommc.modularui.utils.math.functions.classic;

import com.cleanroommc.modularui.api.IMathValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class Ln extends NNFunction {

    public Ln(IMathValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double doubleValue() {
        return Math.log(this.getArg(0).doubleValue());
    }
}