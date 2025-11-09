package com.cleanroommc.modularui.utils;

import java.util.function.DoubleConsumer;

@FunctionalInterface
public interface FloatConsumer extends DoubleConsumer {

    void accept(float value);

    @Override
    default void accept(double value) {
        accept((float) value);
    }
}
