package com.cleanroommc.modularui.utils.math.functions.utility;

import com.cleanroommc.modularui.api.IMathValue;

public class DieRollInteger extends DieRoll {

    public DieRollInteger(IMathValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public double doubleValue() {
        return (int) super.doubleValue();
    }
}