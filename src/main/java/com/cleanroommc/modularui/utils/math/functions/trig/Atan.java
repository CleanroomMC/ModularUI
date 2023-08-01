package com.cleanroommc.modularui.utils.math.functions.trig;

import com.cleanroommc.modularui.api.IMathValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class Atan extends NNFunction {

    public Atan(IMathValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double doubleValue() {
        return Math.atan(this.getArg(0).doubleValue());
    }
}
