package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ByteArraySyncValue extends GenericSyncValue<byte[]> {

    public ByteArraySyncValue(@NotNull Supplier<byte[]> getter, @Nullable Consumer<byte[]> setter) {
        super(getter, setter, ByteBufAdapters.BYTE_ARR, byte[]::clone);
    }
}
