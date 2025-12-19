package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;

import org.jetbrains.annotations.Nullable;

public class CompoundKey extends BaseKey<CompoundKey> {

    private static final IKey[] EMPTY = new IKey[0];

    private final IKey[] keys;

    public CompoundKey(IKey... keys) {
        this.keys = keys == null || keys.length == 0 ? EMPTY : keys;
    }

    @Override
    public String get() {
        return toString(false, null);
    }

    @Override
    public String getFormatted(@Nullable FormattingState parentFormatting) {
        // formatting is prepended to each key
        return toString(true, parentFormatting);
    }

    private String toString(boolean formatted, @Nullable FormattingState parentFormatting) {
        StringBuilder builder = new StringBuilder();
        for (IKey key : this.keys) {
            if (formatted) {
                // merge parent formatting and this formatting to no lose info
                builder.append(key.getFormatted(FormattingState.merge(parentFormatting, getFormatting())));
            } else {
                builder.append(key.get());
            }
        }
        return builder.toString();
    }

    public IKey[] getKeys() {
        return this.keys;
    }
}
