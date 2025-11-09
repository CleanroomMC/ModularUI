package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.utils.ICopy;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BigIntSyncValue extends GenericSyncValue<BigInteger> implements IStringValue<BigInteger> {

    public BigIntSyncValue(@NotNull Supplier<BigInteger> getter, @Nullable Consumer<BigInteger> setter) {
        super(getter, setter, ByteBufAdapters.BIG_INT, ICopy.immutable());
    }

    @Override
    public String getStringValue() {
        return getValue().toString();
    }

    @Override
    public void setStringValue(String val) {
        setValue(new BigInteger(val));
    }
}
