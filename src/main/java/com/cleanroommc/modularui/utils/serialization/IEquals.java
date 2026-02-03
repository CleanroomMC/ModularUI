package com.cleanroommc.modularui.utils.serialization;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A custom function that tests objects for equality.
 *
 * @param <T> object type
 */
public interface IEquals<T> {

    /**
     * Tests two objects for equality. Parameters are not null.
     *
     * @param a first object
     * @param b second object
     * @return true if objects are equal
     */
    boolean areEqual(@NotNull T a, @NotNull T b);

    /**
     * Wraps a {@link IEquals} function to accept nullable parameters.
     *
     * @param equals equals function
     * @param <T>    object type
     * @return null safe equals function
     */
    @SuppressWarnings("ConstantValue")
    static <T> IEquals<T> wrapNullSafe(IEquals<T> equals) {
        return (a, b) -> {
            if (a == null || b == null) return a == b;
            return equals.areEqual(a, b);
        };
    }

    static <T> IEquals<T> defaultTester() {
        return Objects::equals;
    }
}
