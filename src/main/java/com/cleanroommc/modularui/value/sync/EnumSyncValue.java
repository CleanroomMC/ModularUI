package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.IEnumValue;
import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumSyncValue<T extends Enum<T>> extends ValueSyncHandler<T> implements IEnumValue<T>, IIntSyncValue<T> {

    private final Class<T> enumCLass;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private T cache;

    public EnumSyncValue(Class<T> enumCLass, Supplier<T> getter, Consumer<T> setter) {
        this.enumCLass = enumCLass;
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.get();
    }

    @Override
    public Class<T> getEnumClass() {
        return this.enumCLass;
    }

    @Override
    public T getValue() {
        return this.cache;
    }

    @Override
    public void setValue(T value, boolean setSource, boolean sync) {
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
        if (isFirstSync || this.getter.get() != this.cache) {
            setValue(this.getter.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeEnumValue(getValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(buffer.readEnumValue(this.enumCLass), true, false);
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        setValue(this.enumCLass.getEnumConstants()[value], setSource, sync);
    }

    @Override
    public int getIntValue() {
        return this.cache.ordinal();
    }
}
