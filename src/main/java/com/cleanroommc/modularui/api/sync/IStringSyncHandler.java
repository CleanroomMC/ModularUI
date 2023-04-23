package com.cleanroommc.modularui.api.sync;

/**
 * A helper interface for sync values which can be turned into a string.
 *
 * @param <T> value type
 */
public interface IStringSyncHandler<T> extends IValueSyncHandler<T> {

    default String asString(T value) {
        return value.toString();
    }

    T fromString(String value);

    default void updateFromClient(String value) {
        updateFromClient(fromString(value));
    }
}
