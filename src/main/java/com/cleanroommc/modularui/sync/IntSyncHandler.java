package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.sync.INumberSyncHandler;
import com.cleanroommc.modularui.api.sync.IStringSyncHandler;
import com.cleanroommc.modularui.api.sync.ValueSyncHandler;
import net.minecraft.network.PacketBuffer;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntSyncHandler extends ValueSyncHandler<Integer> implements INumberSyncHandler, IStringSyncHandler<Integer> {

    private int cache;
    private final IntSupplier getter;
    private final IntConsumer setter;

    public IntSyncHandler(IntSupplier getter, IntConsumer setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public Integer getCachedValue() {
        return cache;
    }

    public int getInt() {
        return cache;
    }

    @Override
    public void setValue(Integer value) {
        this.cache = value;
        if (setter != null) {
            setter.accept(cache);
        }
    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        return getter != null && (isFirstSync || getter.getAsInt() != cache);
    }

    @Override
    public void updateAndWrite(PacketBuffer buffer) {
        this.cache = getter.getAsInt();
        buffer.writeVarInt(this.cache);
    }

    @Override
    public void read(PacketBuffer buffer) {
        cache = buffer.readVarInt();
        if (setter != null) {
            setter.accept(cache);
        }
    }

    @Override
    public void updateFromClient(Integer value) {
        this.setter.accept(value);
        syncToServer(0, this::updateAndWrite);
    }

    @Override
    public Integer fromString(String value) {
        return Integer.parseInt(value);
    }

    @Override
    public int getCacheAsInt() {
        return this.cache;
    }
}
