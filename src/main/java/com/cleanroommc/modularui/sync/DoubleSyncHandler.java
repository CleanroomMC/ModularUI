package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.sync.IStringSyncHandler;
import com.cleanroommc.modularui.api.sync.ValueSyncHandler;
import net.minecraft.network.PacketBuffer;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DoubleSyncHandler extends ValueSyncHandler<Double> implements IStringSyncHandler<Double> {

    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private double cache;

    public DoubleSyncHandler(DoubleSupplier getter, DoubleConsumer setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.getAsDouble();
    }


    @Override
    public Double getCachedValue() {
        return cache;
    }

    public double getDoubleValue() {
        return cache;
    }

    @Override
    public void setValue(Double value) {
        this.cache = value;
    }

    public void setDoubleValue(double cache) {
        this.cache = cache;
    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        return isFirstSync || this.getter.getAsDouble() != getDoubleValue();
    }

    @Override
    public void updateAndWrite(PacketBuffer buffer) {
        setDoubleValue(this.getter.getAsDouble());
        buffer.writeDouble(getDoubleValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setDoubleValue(buffer.readDouble());
        this.setter.accept(getDoubleValue());
    }

    @Override
    public void updateFromClient(Double value) {
        this.setter.accept(value);
        syncToServer(0, this::updateAndWrite);
    }

    @Override
    public Double fromString(String value) {
        return Double.parseDouble(value);
    }
}
