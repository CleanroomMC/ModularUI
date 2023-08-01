package com.cleanroommc.modularui.api.value.sync;

import com.cleanroommc.modularui.api.value.IIntValue;

/**
 * A helper interface for sync values which can be turned into a integer.
 *
 * @param <T> value type
 */
public interface IIntSyncValue<T> extends IValueSyncHandler<T>, IIntValue<T> {

    default void setIntValue(int val) {
        setIntValue(val, true, true);
    }

    default void setIntValue(int val, boolean setSource) {
        setIntValue(val, setSource, true);
    }

    void setIntValue(int value, boolean setSource, boolean sync);
}
