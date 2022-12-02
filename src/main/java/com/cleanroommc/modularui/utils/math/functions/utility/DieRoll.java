package com.cleanroommc.modularui.utils.math.functions.utility;

import com.cleanroommc.modularui.api.IValue;
import com.cleanroommc.modularui.utils.math.functions.NNFunction;

public class DieRoll extends NNFunction {

    public static double rollDie(int num, double min, double max) {
        double m = Math.max(max, min);
        double n = Math.min(max, min);

        double sum = 0;

        for (int i = 0; i < num; i++) {
            sum += Math.random() * (m - n) + n;
        }

        return sum;
    }

    public DieRoll(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 3;
    }

    @Override
    public double doubleValue() {
        return rollDie((int) this.getArg(0).doubleValue(), this.getArg(1).doubleValue(), this.getArg(2).doubleValue());
    }
}