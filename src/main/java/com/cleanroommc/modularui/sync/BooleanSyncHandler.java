package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.sync.INumberSyncHandler;
import com.cleanroommc.modularui.api.sync.IStringSyncHandler;
import com.cleanroommc.modularui.api.sync.ValueSyncHandler;
import net.minecraft.network.PacketBuffer;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class BooleanSyncHandler extends ValueSyncHandler<Boolean> implements INumberSyncHandler<Boolean>, IStringSyncHandler<Boolean> {

    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private boolean cache;

    public BooleanSyncHandler(BooleanSupplier getter, Consumer<Boolean> setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.getAsBoolean();
    }

    @Override
    public int getCacheAsInt() {
        return this.cache ? 1 : 0;
    }

    @Override
    public Boolean fromInt(int val) {
        return val == 1;
    }

    @Override
    public Boolean fromString(String value) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public Boolean getCachedValue() {
        return cache;
    }

    public boolean getBoolean() {
        return cache;
    }

    @Override
    public void setValue(Boolean value) {
        this.cache = value;
    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        return isFirstSync || this.cache != this.getter.getAsBoolean();
    }

    @Override
    public void updateAndWrite(PacketBuffer buffer) {
        setValue(this.getter.getAsBoolean());
        buffer.writeBoolean(getBoolean());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(buffer.readBoolean());
        this.setter.accept(getBoolean());
    }

    @Override
    public void updateFromClient(Boolean value) {
        this.setter.accept(value);
        syncToServer(0, this::updateAndWrite);
    }
}
