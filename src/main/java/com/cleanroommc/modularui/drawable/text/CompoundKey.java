package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;

import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.Nullable;

public class CompoundKey extends BaseKey {

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
    public String getFormatted(TextFormatting @Nullable [] parentFormatting) {
        // formatting is prepended to each key
        return toString(true, parentFormatting);
    }

    private String toString(boolean formatted, TextFormatting @Nullable [] parentFormatting) {
        StringBuilder builder = new StringBuilder();
        for (IKey key : this.keys) {
            if (formatted) {
                // merge parent formatting and this formatting to no lose info
                builder.append(key.getFormatted(FontRenderHelper.mergeState(parentFormatting, getFormatting())));
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