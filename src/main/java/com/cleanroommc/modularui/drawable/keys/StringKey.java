package com.cleanroommc.modularui.drawable.keys;

import com.cleanroommc.modularui.api.drawable.IKey;

public class StringKey implements IKey {

    public final String string;

    public StringKey(String string) {
        this.string = string;
    }

    @Override
    public String get() {
        return this.string;
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