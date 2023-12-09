package com.cleanroommc.modularui.api.value.sync;

import com.cleanroommc.modularui.api.value.ILongValue;

/**
 * A helper interface for sync values which can be turned into an integer.
 *
 * @param <T> value type
 */
public interface ILongSyncValue<T> extends IValueSyncHandler<T>, ILongValue<T> {

    @Override
    default void setLongValue(long val) {
        setLongValue(val, true, true);
    }

    default void setLongValue(long val, boolean setSource) {
        setLongValue(val, setSource, true);
    }

    void setLongValue(long value, boolean setSource, boolean sync);
}
