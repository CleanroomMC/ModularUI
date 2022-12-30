package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.sync.INumberSyncHandler;
import com.cleanroommc.modularui.api.sync.IStringSyncHandler;
import com.cleanroommc.modularui.api.sync.ValueSyncHandler;
import net.minecraft.network.PacketBuffer;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

public class LongSyncHandler extends ValueSyncHandler<Long> implements INumberSyncHandler<Long>, IStringSyncHandler<Long> {

    private final LongSupplier getter;
    private final LongConsumer setter;
    private long cache;

    public LongSyncHandler(LongSupplier getter, LongConsumer setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.getAsLong();
    }

    @Override
    public int getCacheAsInt() {
        return (int) cache;
    }

    @Override
    public Long fromInt(int val) {
        return (long) val;
    }

    @Override
    public Long fromString(String value) {
        return Long.parseLong(value);
    }

    @Override
    public Long getCachedValue() {
        return this.cache;
    }

    public long getLong() {
        return this.cache;
    }

    @Override
    public void setValue(Long value) {
        this.cache = value;
    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        return isFirstSync || this.getter.getAsLong() != this.cache;
    }

    @Override
    public void updateAndWrite(PacketBuffer buffer) {
        setValue(this.getter.getAsLong());
        buffer.writeVarLong(getLong());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(buffer.readVarLong());
        this.setter.accept(getLong());
    }

    @Override
    public void updateFromClient(Long value) {
        this.setter.accept(value);
        syncToServer(0, this::updateAndWrite);
    }
}
