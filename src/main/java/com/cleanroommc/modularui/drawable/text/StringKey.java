package com.cleanroommc.modularui.drawable.text;

import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StringKey extends BaseKey {

    private final String string;
    private final Object[] args;

    public StringKey(String string) {
        this(string, null);
    }

    public StringKey(String string, @Nullable Object[] args) {
        this.string = Objects.requireNonNull(string);
        this.args = ArrayUtils.isEmpty(args) ? null : args;
    }

    @Override
    public String get() {
        return toString(false, null);
    }

    @Override
    public String getFormatted(@Nullable TextFormatting[] parentFormatting) {
        return toString(true, parentFormatting);
    }

    private String toString(boolean formatted, TextFormatting @Nullable [] parentFormatting) {
        String format = String.format(this.string, FontRenderHelper.fixArgs(this.args, getFormatting(), parentFormatting));
        return formatted ? FontRenderHelper.format(getFormatting(), parentFormatting, format) : format;
    }
}