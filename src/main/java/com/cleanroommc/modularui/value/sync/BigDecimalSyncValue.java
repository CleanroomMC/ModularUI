package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BigDecimalSyncValue extends GenericSyncValue<BigDecimal> {

    public BigDecimalSyncValue(@NotNull Supplier<BigDecimal> getter, @Nullable Consumer<BigDecimal> setter) {
        super(getter, setter, ByteBufAdapters.BIG_DECIMAL, v -> new BigDecimal(v.unscaledValue(), v.scale()));
    }
}
