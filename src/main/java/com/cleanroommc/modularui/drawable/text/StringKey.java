package com.cleanroommc.modularui.drawable.text;

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
        this.args = args == null || args.length == 0 ? null : args;
    }

    @Override
    public String get() {
        if (ArrayUtils.isEmpty(this.args)) return this.string;
        return String.format(this.string, FontRenderHelper.fixArgs(this.args, getFormatting()));
    }
}