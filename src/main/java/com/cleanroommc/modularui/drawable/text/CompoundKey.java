package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;

import com.cleanroommc.modularui.screen.ClientScreenHandler;

import net.minecraft.util.text.TextFormatting;

import org.jetbrains.annotations.Nullable;

public class CompoundKey extends BaseKey {

    private static final IKey[] EMPTY = new IKey[0];

    private final IKey[] keys;
    private String string;
    private long time = 0;

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
        if (ClientScreenHandler.getTicks() != this.time) {
            StringBuilder builder = new StringBuilder();
            for (IKey key : this.keys) {
                if (formatted) {
                    // merge parent formatting and this formatting to no lose info
                    builder.append(key.getFormatted(FontRenderHelper.mergeState(parentFormatting, getFormatting())));
                } else {
                    builder.append(key.getFormatted());
                }
            }
            this.string = builder.toString();
        }
        return this.string;
    }

    public IKey[] getKeys() {
        return this.keys;
    }
}