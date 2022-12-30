package com.cleanroommc.modularui.api.sync;

public interface INumberSyncHandler<T> extends IValueSyncHandler<T> {

    int getCacheAsInt();

    T fromInt(int val);

    default void updateFromClient(int value) {
        updateFromClient(fromInt(value));
    }
}
