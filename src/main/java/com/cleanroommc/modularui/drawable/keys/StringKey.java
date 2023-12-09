package com.cleanroommc.modularui.drawable.keys;

import com.cleanroommc.modularui.api.drawable.IKey;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StringKey implements IKey {

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
        return this.args == null ? this.string : String.format(this.string, this.args);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof StringKey) {
            return this.string.equals(((StringKey) obj).string);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.string;
    }
}