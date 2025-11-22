package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IKey;

import org.jetbrains.annotations.Nullable;

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
        return toString(false, null);
    }

    @Override
    public String getFormatted(@Nullable FormattingState parentFormatting) {
        // formatting is prepended to each key
        return toString(true, parentFormatting);
    }

    private String toString(boolean formatted, @Nullable FormattingState parentFormatting) {
        IKey key = this.supplier.get();
        if (formatted) {
            // merge parent formatting and this formatting to no lose info
            return key.getFormatted(FormattingState.merge(parentFormatting, getFormatting()));
        } else {
            return key.get();
        }
    }
}
