package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IByteSyncValue;

import com.cleanroommc.modularui.network.NetworkUtils;

import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ByteSyncValue extends ValueSyncHandler<Byte> implements IByteSyncValue<Byte> {

    private byte cache;
    private final ByteSupplier getter;
    private final ByteConsumer setter;

    public ByteSyncValue(@NotNull ByteSupplier getter) {
        this(getter, (ByteConsumer) null);
    }

    public ByteSyncValue(@NotNull ByteSupplier getter, @Nullable ByteConsumer setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.getByte();
    }

    @Contract("null, _, null, _ -> fail")
    public ByteSyncValue(@Nullable ByteSupplier clientGetter, @Nullable ByteConsumer clientSetter,
                        @Nullable ByteSupplier serverGetter, @Nullable ByteConsumer serverSetter) {
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
        this.cache = this.getter.getByte();
    }

    public ByteSyncValue(@Nullable ByteSupplier clientGetter,
                         @Nullable ByteSupplier serverGetter) {
        this(clientGetter, null, serverGetter, null);
    }

    @Override
    public void setValue(Byte value, boolean setSource, boolean sync) {
        setByteValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        return false;
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeByte(getByteValue());
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        setByteValue(buffer.readByte(), false, false);
    }

    @Override
    public Byte getValue() {
        return getByteValue();
    }

    @Override
    public void setByteValue(byte value, boolean setSource, boolean sync) {
        this.cache = value;
        if (setSource && this.setter != null) {
            this.setter.setByte(value);
        }
        if (sync) {
            sync(0, this::write);
        }
    }

    @Override
    public byte getByteValue() {
        return this.cache;
    }

    public interface ByteSupplier {
        byte getByte();
    }
    public interface ByteConsumer {
        void setByte(byte b);
    }
}
