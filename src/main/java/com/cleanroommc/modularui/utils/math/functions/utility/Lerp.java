package com.cleanroommc.modularui.utils.math.functions.utility;

import com.cleanroommc.modularui.api.IValue;
import com.cleanroommc.modularui.utils.Interpolations;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class Lerp extends NNFunction {

    public Lerp(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 3;
    }

    @Override
    public double doubleValue() {
        return Interpolations.lerp(this.getArg(0).doubleValue(), this.getArg(1).doubleValue(), this.getArg(2).doubleValue());
    }
}