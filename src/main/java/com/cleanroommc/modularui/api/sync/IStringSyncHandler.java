package com.cleanroommc.modularui.api.sync;

public interface IStringSyncHandler<T> extends IValueSyncHandler<T> {

    default String asString(T value) {
        return value.toString();
    }

    T fromString(String value);

    default void updateFromClient(String value) {
        updateFromClient(fromString(value));
    }
}
