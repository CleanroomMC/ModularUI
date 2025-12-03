package com.cleanroommc.modularui.api.value;

import org.jetbrains.annotations.Nullable;

public interface ISyncOrValue {

    ISyncOrValue EMPTY = new ISyncOrValue() {
        @Override
        public <T> @Nullable T castNullable(Class<T> type) {
            return null;
        }

        @Override
        public boolean isTypeOrEmpty(Class<?> type) {
            return true;
        }
    };

    static ISyncOrValue orEmpty(@Nullable ISyncOrValue syncOrValue) {
        return syncOrValue != null ? syncOrValue : EMPTY;
    }

    default boolean isTypeOrEmpty(Class<?> type) {
        return type.isAssignableFrom(getClass());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    default <T> T castNullable(Class<T> type) {
        return type.isAssignableFrom(getClass()) ? (T) this : null;
    }

    default <V> IValue<V> castValueNullable(Class<V> valueType) {
        return null;
    }

    default <T> T castOrThrow(Class<T> type) {
        T t = castNullable(type);
        if (t == null) {
            if (!isSyncHandler() && !isValueHandler()) {
                throw new IllegalStateException("Empty sync handler or value can't be used for anything.");
            }
            String self = isSyncHandler() ? "sync handler" : "value";
            throw new IllegalStateException("Can't cast " + self + " of type '" + getClass().getSimpleName() + "' to type '" + type.getSimpleName() + "'.");
        }
        return t;
    }

    default boolean isValueOfType(Class<?> type) {
        return false;
    }

    default boolean isSyncHandler() {
        return false;
    }

    default boolean isValueHandler() {
        return false;
    }
}
