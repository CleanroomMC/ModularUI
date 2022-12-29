package com.cleanroommc.modularui.api.sync;

public interface IStringValueSyncHandler<T> extends IValueSyncHandler<T> {

    default String asString(T value) {
        return value.toString();
    }

    T fromString(String value);

    default void updateFromClient(String value) {
        updateFromClient(fromString(value));
    }
}
