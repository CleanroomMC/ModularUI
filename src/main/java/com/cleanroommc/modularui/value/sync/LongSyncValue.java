package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.api.value.sync.ILongSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import net.minecraft.network.PacketBuffer;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

public class LongSyncValue extends ValueSyncHandler<Long> implements ILongSyncValue<Long>, IIntSyncValue<Long>, IStringSyncValue<Long> {

    private final LongSupplier getter;
    private final LongConsumer setter;
    private long cache;

    public LongSyncValue(LongSupplier getter, LongConsumer setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.getAsLong();
    }

    @Override
    public Long getValue() {
        return this.cache;
    }

    @Override
    public long getLongValue() {
        return this.cache;
    }

    @Override
    public void setValue(Long value, boolean setSource, boolean sync) {
        setLongValue(value, setSource, sync);
    }

    @Override
    public void setLongValue(long value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        return isFirstSync || this.getter.getAsLong() != this.cache;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeVarLong(getLongValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(buffer.readVarLong(), true, false);
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        setLongValue(value, setSource, sync);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setLongValue(Long.parseLong(value), setSource, sync);
    }

    @Override
    public int getIntValue() {
        return (int) this.cache;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }
}
