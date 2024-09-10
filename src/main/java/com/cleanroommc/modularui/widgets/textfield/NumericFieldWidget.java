package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IMathValue;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.utils.math.MathBuilder;
import com.cleanroommc.modularui.value.StringValue;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public abstract class NumericFieldWidget<W extends NumericFieldWidget<W>> extends OneLineTextField<W> {

    protected final DecimalFormat format = new DecimalFormat();
    private String mathFailMessage = null;

    public double parse(String num) {
        try {
            IMathValue mathValue = MathBuilder.INSTANCE.parse(num);
            double ret = mathValue.doubleValue();
            this.mathFailMessage = null;
            return ret;
        } catch (MathBuilder.ParseException | IMathValue.EvaluateException e) {
            this.mathFailMessage = e.getMessage();
        } catch (Exception e) {
            this.mathFailMessage = "Internal crash";
            ModularUI.LOGGER.catching(e);
        }
        return getNumberFromValue().doubleValue();
    }

    public IStringValue<?> createMathFailMessageValue() {
        return new StringValue.Dynamic(() -> this.mathFailMessage, val -> this.mathFailMessage = val);
    }

    @NotNull
    @Override
    protected final String getDisplayTextFromValue() {
        return format.format(getNumberFromValue());
    }

    protected abstract Number getNumberFromValue();
}
