package com.cleanroommc.modularui.api.value.sync;

import com.cleanroommc.modularui.api.value.IBoolValue;

/**
 * A helper interface for sync values which can be turned into a integer.
 *
 * @param <T> value type
 */
public interface IBoolSyncValue<T> extends IValueSyncHandler<T>, IBoolValue<T> {

    @Override
    default void setBoolValue(boolean val) {
        setBoolValue(val, true, true);
    }

    default void setBoolValue(boolean val, boolean setSource) {
        setBoolValue(val, setSource, true);
    }

    void setBoolValue(boolean value, boolean setSource, boolean sync);
}
