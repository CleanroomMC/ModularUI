package com.cleanroommc.modularui.api.value.sync;

import com.cleanroommc.modularui.api.value.IDoubleValue;

/**
 * A helper interface for sync values which can be turned into a integer.
 *
 * @param <T> value type
 */
public interface IDoubleSyncValue<T> extends IValueSyncHandler<T>, IDoubleValue<T> {

    default void setDoubleValue(double val) {
        setDoubleValue(val, true, true);
    }

    default void setDoubleValue(double val, boolean setSource) {
        setDoubleValue(val, setSource, true);
    }

    void setDoubleValue(double value, boolean setSource, boolean sync);
}
