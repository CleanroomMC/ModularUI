package com.cleanroommc.modularui.api.value.sync;

import com.cleanroommc.modularui.api.value.IFloatValue;

/**
 * A helper interface for sync values which can be turned into an integer.
 *
 * @param <T> value type
 */
public interface IFloatSyncValue<T> extends IValueSyncHandler<T>, IFloatValue<T> {

    @Override
    default void setFloatValue(float val) {
        setFloatValue(val, true, true);
    }

    default void setFloatValue(float val, boolean setSource) {
        setFloatValue(val, setSource, true);
    }

    void setFloatValue(float value, boolean setSource, boolean sync);
}
