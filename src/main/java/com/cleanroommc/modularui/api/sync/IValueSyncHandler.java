package com.cleanroommc.modularui.api.sync;

import net.minecraft.network.PacketBuffer;

public interface IValueSyncHandler<T> {

    T getCachedValue();

    void setValue(T value);

    boolean needsSync(boolean isFirstSync);

    void updateAndWrite(PacketBuffer buffer);

    void read(PacketBuffer buffer);

    void updateFromClient(T value);
}
