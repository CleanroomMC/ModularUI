package com.cleanroommc.modularui.utils.math.functions.rounding;

import com.cleanroommc.modularui.api.sync.IValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class Round extends NNFunction {

    public Round(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double doubleValue() {
        return Math.round(this.getArg(0).doubleValue());
    }
}