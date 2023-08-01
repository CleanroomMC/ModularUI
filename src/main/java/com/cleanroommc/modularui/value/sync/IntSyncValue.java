package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import net.minecraft.network.PacketBuffer;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntSyncValue extends ValueSyncHandler<Integer> implements IIntSyncValue<Integer>, IStringSyncValue<Integer> {

    private int cache;
    private final IntSupplier getter;
    private final IntConsumer setter;

    public IntSyncValue(IntSupplier getter, IntConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Integer getValue() {
        return this.cache;
    }

    @Override
    public int getIntValue() {
        return this.cache;
    }

    @Override
    public void setValue(Integer value, boolean setSource, boolean sync) {
        setIntValue(value, setSource, sync);
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (this.getter != null && (isFirstSync || this.getter.getAsInt() != this.cache)) {
            setIntValue(this.getter.getAsInt(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeVarInt(this.cache);
    }

    @Override
    public void read(PacketBuffer buffer) {
        setIntValue(buffer.readVarInt(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setIntValue(Integer.parseInt(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }
}
