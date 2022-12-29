package com.cleanroommc.modularui.utils.math.functions.string;

import com.cleanroommc.modularui.api.sync.IValue;
import com.cleanroommc.modularui.utils.math.functions.SNFunction;

public class StringStartsWith extends SNFunction {

    public StringStartsWith(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    public double doubleValue() {
        return this.getArg(0).stringValue().startsWith(this.getArg(1).stringValue()) ? 1 : 0;
    }
}
