package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.ValueSyncHandler;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntSyncHandler extends ValueSyncHandler<Integer> {

    private int cache;
    @Nullable
    private final IntSupplier getter;
    @Nullable
    private final IntConsumer setter;

    public IntSyncHandler(@Nullable IntSupplier getter, @Nullable IntConsumer setter) {
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
    public void write(PacketBuffer buffer) {
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
}
