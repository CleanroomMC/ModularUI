package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.api.value.sync.IBoolSyncValue;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Accepts enums which have exactly two elements. Can act as {@link IBoolSyncValue}.
 */
public class BinaryEnumSyncValue<T extends Enum<T>> extends EnumSyncValue<T> implements IBoolSyncValue<T> {

    public BinaryEnumSyncValue(Class<T> enumClass, Supplier<T> getter, Consumer<T> setter) {
        super(enumClass, getter, setter);
        if (enumClass.getEnumConstants().length != 2) {
            throw new IllegalArgumentException("Enum class must have exactly two elements");
        }
    }

    public BinaryEnumSyncValue(Class<T> enumClass, @Nullable Supplier<T> clientGetter, @Nullable Consumer<T> clientSetter, @Nullable Supplier<T> serverGetter, @Nullable Consumer<T> serverSetter) {
        super(enumClass, clientGetter, clientSetter, serverGetter, serverSetter);
        if (enumClass.getEnumConstants().length != 2) {
            throw new IllegalArgumentException("Enum class must have exactly two elements");
        }
    }

    @Override
    public boolean getBoolValue() {
        return this.cache.ordinal() == 1;
    }

    @Override
    public void setBoolValue(boolean value, boolean setSource, boolean sync) {
        setValue(this.enumClass.getEnumConstants()[value ? 1 : 0], setSource, sync);
    }
}
