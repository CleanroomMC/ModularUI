package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IBoolSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.BooleanConsumer;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public class BooleanSyncValue extends ValueSyncHandler<Boolean> implements IBoolSyncValue<Boolean>, IStringSyncValue<Boolean> {

    private final BooleanSupplier getter;
    private final BooleanConsumer setter;
    private boolean cache;

    public BooleanSyncValue(@NotNull BooleanSupplier getter, @Nullable BooleanConsumer setter) {
        this.getter = Objects.requireNonNull(getter);
        this.setter = setter;
        this.cache = getter.getAsBoolean();
    }

    public BooleanSyncValue(@NotNull BooleanSupplier getter) {
        this(getter, (BooleanConsumer) null);
    }

    @Contract("null, null -> fail")
    public BooleanSyncValue(@Nullable BooleanSupplier clientGetter,
                            @Nullable BooleanSupplier serverGetter) {
        this(clientGetter, null, serverGetter, null);
    }

    @Contract("null, _, null, _ -> fail")
    public BooleanSyncValue(@Nullable BooleanSupplier clientGetter, @Nullable BooleanConsumer clientSetter,
                            @Nullable BooleanSupplier serverGetter, @Nullable BooleanConsumer serverSetter) {
        if (clientGetter == null && serverGetter == null) {
            throw new NullPointerException("Client or server getter must not be null!");
        }
        if (NetworkUtils.isClient()) {
            this.getter = clientGetter != null ? clientGetter : serverGetter;
            this.setter = clientSetter != null ? clientSetter : serverSetter;
        } else {
            this.getter = serverGetter != null ? serverGetter : clientGetter;
            this.setter = serverSetter != null ? serverSetter : clientSetter;
        }
        this.cache = this.getter.getAsBoolean();
    }

    @Override
    public Boolean getValue() {
        return this.cache;
    }

    @Override
    public boolean getBoolValue() {
        return this.cache;
    }

    @Override
    public void setValue(@NotNull Boolean value, boolean setSource, boolean sync) {
        setBoolValue(value, setSource, sync);
    }

    @Override
    public void setBoolValue(boolean value, boolean setSource, boolean sync) {
        this.cache = value;
        onValueChanged();
        if (setSource && this.setter != null) {
            this.setter.accept(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.getter.getAsBoolean() != this.cache) {
            setBoolValue(this.getter.getAsBoolean(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setBoolValue(this.getter.getAsBoolean(), false, true);
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
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setBoolValue(Boolean.getBoolean(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }
}
