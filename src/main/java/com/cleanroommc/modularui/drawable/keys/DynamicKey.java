package com.cleanroommc.modularui.drawable.keys;

import com.cleanroommc.modularui.api.drawable.IKey;

import java.util.function.Supplier;

public class DynamicKey implements IKey {

    private final Supplier<String> supplier;

    public DynamicKey(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    @Override
    public String get() {
        return supplier.get();
    }

    @Override
    public void set(String string) {

    }
}
