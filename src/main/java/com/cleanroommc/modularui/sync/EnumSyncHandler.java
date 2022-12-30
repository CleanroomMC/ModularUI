package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.sync.INumberSyncHandler;
import com.cleanroommc.modularui.api.sync.ValueSyncHandler;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumSyncHandler<T extends Enum<T>> extends ValueSyncHandler<T> implements INumberSyncHandler<T> {

    private final Class<T> enumCLass;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private T cache;

    public EnumSyncHandler(Class<T> enumCLass, Supplier<T> getter, Consumer<T> setter) {
        this.enumCLass = enumCLass;
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.get();
    }

    @Override
    public int getCacheAsInt() {
        return this.cache.ordinal();
    }

    @Override
    public T fromInt(int val) {
        return this.enumCLass.getEnumConstants()[val];
    }

    @Override
    public T getCachedValue() {
        return this.cache;
    }

    @Override
    public void setValue(T value) {
        this.cache = value;
    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        return isFirstSync || this.cache != this.getter.get();
    }

    @Override
    public void updateAndWrite(PacketBuffer buffer) {
        setValue(this.getter.get());
        buffer.writeEnumValue(getCachedValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(buffer.readEnumValue(this.enumCLass));
        this.setter.accept(getCachedValue());
    }

    @Override
    public void updateFromClient(T value) {
        this.setter.accept(value);
        syncToServer(0, this::updateAndWrite);
    }
}
