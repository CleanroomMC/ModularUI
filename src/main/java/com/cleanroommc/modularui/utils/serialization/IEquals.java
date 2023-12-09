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
     * @param t1 first object
     * @param t2 second object
     * @return true if objects are equal
     */
    boolean areEqual(@NotNull T t1, @NotNull T t2);

    /**
     * Wraps a {@link IEquals} function to accept nullable parameters.
     *
     * @param equals equals function
     * @param <T>    object type
     * @return null safe equals function
     */
    @SuppressWarnings("ConstantValue")
    static <T> IEquals<T> wrapNullSafe(IEquals<T> equals) {
        return (t1, t2) -> {
            if (t1 == null || t2 == null) return t1 == t2;
            return equals.areEqual(t1, t2);
        };
    }

    static <T> IEquals<T> defaultTester() {
        return Objects::equals;
    }
}
