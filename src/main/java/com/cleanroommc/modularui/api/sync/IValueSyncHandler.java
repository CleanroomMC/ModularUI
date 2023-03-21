package com.cleanroommc.modularui.api.sync;

import net.minecraft.network.PacketBuffer;

/**
 * A helper interface for syncing an object value.
 *
 * @param <T> object value type
 */
public interface IValueSyncHandler<T> {

    T getCachedValue();

    void setValue(T value);

    boolean needsSync(boolean isFirstSync);

    void updateAndWrite(PacketBuffer buffer);

    void read(PacketBuffer buffer);

    void updateFromClient(T value);
}
