package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IBoolSyncValue;
import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class BooleanSyncValue extends ValueSyncHandler<Boolean> implements IBoolSyncValue<Boolean>, IIntSyncValue<Boolean>, IStringSyncValue<Boolean> {

    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private boolean cache;

    public BooleanSyncValue(BooleanSupplier getter, Consumer<Boolean> setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.getAsBoolean();
    }

    @Override
    public Boolean getValue() {
        return cache;
    }

    @Override
    public boolean getBoolValue() {
        return cache;
    }

    @Override
    public void setValue(@NotNull Boolean value, boolean setSource, boolean sync) {
        setBoolValue(value, setSource, sync);
    }

    @Override
    public void setBoolValue(boolean value, boolean setSource, boolean sync) {
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
        return isFirstSync || this.cache != this.getter.getAsBoolean();
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeBoolean(getBoolValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setBoolValue(buffer.readBoolean(), true, false);
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        setBoolValue(value == 1, setSource, sync);
    }

    @Override
    public int getIntValue() {
        return this.cache ? 1 : 0;
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setBoolValue(Boolean.getBoolean(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }
}
