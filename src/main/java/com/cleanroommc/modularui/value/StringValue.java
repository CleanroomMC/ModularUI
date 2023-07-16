package com.cleanroommc.modularui.value;

import com.cleanroommc.modularui.api.value.IStringValue;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringValue extends ConstValue<String> implements IStringValue<String> {

    public StringValue(String value) {
        super(value);
    }

    @Override
    public String getStringValue() {
        return getValue();
    }

    @Override
    public void setStringValue(String val, boolean setSource) {
        setValue(val, setSource);
    }

    public static class Dynamic extends DynamicValue<String> implements IStringValue<String> {

        public Dynamic(Supplier<String> getter, @Nullable Consumer<String> setter) {
            super(getter, setter);
        }

        @Override
        public String getStringValue() {
            return getValue();
        }

        @Override
        public void setStringValue(String val, boolean setSource) {
            setValue(val, setSource);
        }
    }
}
