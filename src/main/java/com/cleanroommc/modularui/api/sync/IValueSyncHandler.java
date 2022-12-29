package com.cleanroommc.modularui.api.sync;

import net.minecraft.network.PacketBuffer;

public interface IValueSyncHandler<T> {

    T getCachedValue();

    void setValue(T value);

    boolean needsSync(boolean isFirstSync);

    void updateAndWrite(PacketBuffer buffer);

    void read(PacketBuffer buffer);

    void updateFromClient(T value);

    interface IStringValueSyncHandler<T> extends IValueSyncHandler<T> {

        default String asString(T value) {
            return asString(value);
        }

        T fromString(String value);

        default void updateFromClient(String value) {
            updateFromClient(fromString(value));
        }
    }
}
