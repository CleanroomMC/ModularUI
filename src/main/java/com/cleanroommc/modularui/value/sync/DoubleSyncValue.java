package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IDoubleSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DoubleSyncValue extends ValueSyncHandler<Double> implements IDoubleSyncValue<Double>, IStringSyncValue<Double> {

    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private double cache;

    public DoubleSyncValue(DoubleSupplier getter, DoubleConsumer setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.getAsDouble();
    }

    @Override
    public Double getValue() {
        return this.cache;
    }

    @Override
    public double getDoubleValue() {
        return this.cache;
    }

    @Override
    public void setValue(@NotNull Double value, boolean setSource, boolean sync) {
        setDoubleValue(value, setSource, sync);
    }

    @Override
    public void setDoubleValue(double value, boolean setSource, boolean sync) {
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
        if (isFirstSync || this.getter.getAsDouble() != this.cache) {
            setDoubleValue(this.getter.getAsDouble(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeDouble(getDoubleValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setDoubleValue(buffer.readDouble(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setDoubleValue(Double.parseDouble(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }
}
