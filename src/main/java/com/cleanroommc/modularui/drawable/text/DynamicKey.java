package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.screen.ClientScreenHandler;

import java.util.Objects;
import java.util.function.Supplier;

public class DynamicKey extends BaseKey {

    private final Supplier<String> supplier;
    private String cache;
    private long time = 0;

    public DynamicKey(Supplier<String> supplier) {
        Objects.requireNonNull(supplier.get(), "IKey returns a null string!");
        this.supplier = supplier;
    }

    @Override
    public String get() {
        if (ClientScreenHandler.getTicks() != this.time) {
            this.time = ClientScreenHandler.getTicks();
            this.cache = this.supplier.get();
        }
        return this.cache;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof DynamicKey dynamicKey && Objects.equals(dynamicKey.cache, this.cache));
    }
}
