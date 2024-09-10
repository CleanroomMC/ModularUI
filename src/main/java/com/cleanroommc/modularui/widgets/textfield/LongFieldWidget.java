package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.api.value.ILongValue;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.value.LongValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import java.util.function.LongSupplier;
import java.util.function.LongUnaryOperator;

public class LongFieldWidget extends NumericFieldWidget<LongFieldWidget> {

    private ILongValue<?> longValue;
    private LongUnaryOperator validator = val -> val;

    @Override
    protected void setupValueIfNull() {
        if (this.longValue == null) {
            this.longValue = new LongValue(0);
        }
    }

    @Override
    protected boolean checkAndSetSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof ILongValue<?> val) {
            this.longValue = val;
            return true;
        }
        return false;
    }

    @Override
    protected Number getNumberFromValue() {
        return this.longValue.getLongValue();
    }

    @Override
    protected void parseDisplayText(String text) {
        double parsedVal = parse(text);
        long validatedVal = this.validator.applyAsLong((long) parsedVal);
        this.longValue.setLongValue(validatedVal);
    }

    public LongFieldWidget value(ILongValue<?> value) {
        this.longValue = value;
        setValue(value);
        return this;
    }

    public LongFieldWidget setValidator(LongUnaryOperator validator) {
        this.validator = validator;
        return this;
    }

    public LongFieldWidget setRange(long min, long max) {
        return setValidator(val -> MathUtils.clamp(val, min, max));
    }

    public LongFieldWidget setRange(LongSupplier min, LongSupplier max) {
        return setValidator(val -> MathUtils.clamp(val, min.getAsLong(), max.getAsLong()));
    }
}
