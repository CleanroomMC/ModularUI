package com.cleanroommc.modularui.utils.math.functions.utility;

import com.cleanroommc.modularui.api.sync.IValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class HermiteBlend extends NNFunction {

    public HermiteBlend(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double doubleValue() {
        double x = this.getArg(0).doubleValue();

        return 3 * x * x - 2 * x * x * x;
    }
}