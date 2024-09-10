package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;

public class IntFieldWidget extends NumericFieldWidget<IntFieldWidget> {

    private IIntValue<?> intValue;
    private IntUnaryOperator validator = val -> val;

    @Override
    protected void setupValueIfNull() {
        if (this.intValue == null) {
            this.intValue = new IntValue(0);
        }
    }

    @Override
    protected boolean checkAndSetSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof IIntValue<?> val) {
            this.intValue = val;
            return true;
        }
        return false;
    }

    @Override
    protected Number getNumberFromValue() {
        return this.intValue.getIntValue();
    }

    @Override
    protected void parseDisplayText(String text) {
        double parsedVal = parse(text);
        int validatedVal = this.validator.applyAsInt((int) parsedVal);
        this.intValue.setIntValue(validatedVal);
    }

    public IntFieldWidget value(IIntValue<?> value) {
        this.intValue = value;
        setValue(value);
        return this;
    }

    public IntFieldWidget setValidator(IntUnaryOperator validator) {
        this.validator = validator;
        return this;
    }

    public IntFieldWidget setRange(int min, int max) {
        return setValidator(val -> MathUtils.clamp(val, min, max));
    }

    public IntFieldWidget setRange(IntSupplier min, IntSupplier max) {
        return setValidator(val -> MathUtils.clamp(val, min.getAsInt(), max.getAsInt()));
    }
}
