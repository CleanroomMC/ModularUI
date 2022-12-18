package com.cleanroommc.modularui.api;

import net.minecraft.network.PacketBuffer;

public interface IValueSyncHandler<T>  {

    T getCachedValue();

    void setValue(T value);

    boolean needsSync(boolean isFirstSync);

    void updateAndWrite(PacketBuffer buffer);

    void read(PacketBuffer buffer);
}
