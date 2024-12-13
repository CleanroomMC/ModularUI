package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ClientScreenHandler;

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
        if (ClientScreenHandler.getTicks() != this.time) {
            this.time = ClientScreenHandler.getTicks();
            StringBuilder builder = new StringBuilder();
            for (IKey key : this.keys) {
                builder.append(key.get());
            }
            this.string = builder.toString();
        }
        return this.string;
    }

    public IKey[] getKeys() {
        return this.keys;
    }
}