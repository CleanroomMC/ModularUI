package com.cleanroommc.modularui.utils.math.functions.utility;

import com.cleanroommc.modularui.api.IMathValue;

public class RandomInteger extends Random {

    public RandomInteger(IMathValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public double doubleValue() {
        return (int) super.doubleValue();
    }
}