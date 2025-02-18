package com.cleanroommc.modularui.drawable.text;

import java.util.Objects;
import java.util.function.Supplier;

public class DynamicKey extends BaseKey {

    private final Supplier<String> supplier;

    public DynamicKey(Supplier<String> supplier) {
        Objects.requireNonNull(supplier.get(), "IKey returns a null string!");
        this.supplier = supplier;
    }

    @Override
    public String get() {
        return this.supplier.get();
    }
}
