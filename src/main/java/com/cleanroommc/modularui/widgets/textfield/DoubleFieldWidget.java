package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.api.value.IDoubleValue;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

public class DoubleFieldWidget extends NumericFieldWidget<DoubleFieldWidget> {

    private IDoubleValue<?> doubleValue;
    private DoubleUnaryOperator validator = val -> val;

    @Override
    protected void setupValueIfNull() {
        if (this.doubleValue == null) {
            this.doubleValue = new DoubleValue(0);
        }
    }

    @Override
    protected boolean checkAndSetSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof IDoubleValue<?> val) {
            this.doubleValue = val;
            return true;
        }
        return false;
    }

    @Override
    protected Number getNumberFromValue() {
        return this.doubleValue.getDoubleValue();
    }

    @Override
    protected void parseDisplayText(String text) {
        double parsedVal = parse(text);
        double validatedVal = this.validator.applyAsDouble(parsedVal);
        this.doubleValue.setDoubleValue(validatedVal);
    }

    public DoubleFieldWidget value(IDoubleValue<?> value) {
        this.doubleValue = value;
        setValue(value);
        return this;
    }

    public DoubleFieldWidget setValidator(DoubleUnaryOperator validator) {
        this.validator = validator;
        return this;
    }

    public DoubleFieldWidget setRange(double min, double max) {
        return setValidator(val -> MathUtils.clamp(val, min, max));
    }

    public DoubleFieldWidget setRange(DoubleSupplier min, DoubleSupplier max) {
        return setValidator(val -> MathUtils.clamp(val, min.getAsDouble(), max.getAsDouble()));
    }
}
