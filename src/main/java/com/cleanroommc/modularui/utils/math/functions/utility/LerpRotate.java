package com.cleanroommc.modularui.utils.math.functions.utility;

import com.cleanroommc.modularui.api.IMathValue;
import com.cleanroommc.modularui.utils.Interpolations;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class LerpRotate extends NNFunction {

    public LerpRotate(IMathValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 3;
    }

    @Override
    public double doubleValue() {
        return Interpolations.lerpYaw(this.getArg(0).doubleValue(), this.getArg(1).doubleValue(), this.getArg(2).doubleValue());
    }
}