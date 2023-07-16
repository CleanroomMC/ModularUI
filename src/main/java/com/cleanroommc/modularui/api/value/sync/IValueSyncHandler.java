package com.cleanroommc.modularui.api.value.sync;

import com.cleanroommc.modularui.api.value.IValue;
import net.minecraft.network.PacketBuffer;

/**
 * A helper interface for syncing an object value.
 *
 * @param <T> object value type
 */
public interface IValueSyncHandler<T> extends IValue<T> {

    @Override
    default void setValue(T value) {
        setValue(value, true, true);
    }

    @Override
    default void setValue(T value, boolean setSource) {
        setValue(value, setSource, true);
    }

    void setValue(T value, boolean setSource, boolean sync);

    boolean needsSync(boolean isFirstSync);

    void write(PacketBuffer buffer);

    void read(PacketBuffer buffer);
}
