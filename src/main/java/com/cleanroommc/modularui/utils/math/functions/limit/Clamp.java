package com.cleanroommc.modularui.utils.math.functions.limit;

import com.cleanroommc.modularui.api.IMathValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;
import net.minecraft.util.math.MathHelper;

public class Clamp extends NNFunction {

    public Clamp(IMathValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 3;
    }

    @Override
    public double doubleValue() {
        return MathHelper.clamp(this.getArg(0).doubleValue(), this.getArg(1).doubleValue(), this.getArg(2).doubleValue());
    }
}