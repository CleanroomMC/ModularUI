package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class TextFieldWidget extends OneLineTextField<TextFieldWidget> {

    private IStringValue<?> stringValue;
    private Function<String, String> validator = val -> val;

    @Override
    protected void setupValueIfNull() {
        if (this.stringValue == null) {
            this.stringValue = new StringValue("");
        }
    }

    @Override
    protected boolean checkAndSetSyncHandler(SyncHandler syncHandler) {
        if (syncHandler instanceof IStringValue<?> val) {
            this.stringValue = val;
            return true;
        }
        return false;
    }

    @NotNull
    @Override
    protected String getDisplayTextFromValue() {
        return this.stringValue.getStringValue();
    }

    @Override
    protected void parseDisplayText(String text) {
        String validatedText = this.validator.apply(text);
        this.stringValue.setStringValue(validatedText);
    }

    public TextFieldWidget value(IStringValue<?> value) {
        this.stringValue = value;
        setValue(value);
        return this;
    }

    public TextFieldWidget setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }
}
