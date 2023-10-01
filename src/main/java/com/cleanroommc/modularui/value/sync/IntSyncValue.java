package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.network.NetworkUtils;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
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

    @Contract("null, _, null, _ -> fail")
    public IntSyncValue(@Nullable IntSupplier clientGetter, @Nullable IntConsumer clientSetter,
                           @Nullable IntSupplier serverGetter, @Nullable IntConsumer serverSetter) {
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
        this.cache = this.getter.getAsInt();
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
