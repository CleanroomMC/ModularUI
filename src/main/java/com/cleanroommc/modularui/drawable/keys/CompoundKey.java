package com.cleanroommc.modularui.drawable.keys;

import com.cleanroommc.modularui.ClientEventHandler;
import com.cleanroommc.modularui.api.drawable.IKey;

public class CompoundKey implements IKey {

    private static final IKey[] EMPTY = new IKey[0];

    private final IKey[] keys;
    private String string;
    private long time = 0;

    public CompoundKey(IKey... keys) {
        this.keys = keys == null || keys.length == 0 ? EMPTY : keys;
    }

    @Override
    public String get() {
        if (ClientEventHandler.getTicks() != this.time) {
            this.time = ClientEventHandler.getTicks();
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof CompoundKey) {
            return this.get().equals(((CompoundKey) obj).get());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.get();
    }
}