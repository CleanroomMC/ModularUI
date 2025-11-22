package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;

import java.util.Objects;
import java.util.function.Supplier;

public class DynamicKey extends BaseKey {

    private final Supplier<IKey> supplier;

    public DynamicKey(Supplier<IKey> supplier) {
        Objects.requireNonNull(supplier.get(), "IKey returns a null key!");
        this.supplier = supplier;
    }

    @Override
    public String get() {
        return this.supplier.get().getFormatted(getFormatting());
    }
}
