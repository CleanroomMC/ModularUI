package com.cleanroommc.modularui.api.sync;

/**
 * A helper interface for sync values which can be turned into a integer.
 *
 * @param <T> value type
 */
public interface INumberSyncHandler<T> extends IValueSyncHandler<T> {

    int getCacheAsInt();

    T fromInt(int val);

    default void updateFromClient(int value) {
        updateFromClient(fromInt(value));
    }
}
