package com.cleanroommc.modularui.animation;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface IAnimator {

    @Nullable IAnimator getParent();

    void animate(boolean reverse);

    default void animate() {
        animate(false);
    }

    void stop();

    void reset(boolean atEnd);

    default void reset() {
        reset(false);
    }

    /**
     * Advances the animation by a given duration.
     *
     * @param elapsedTime elapsed time in ms
     * @return remaining time (elapsed time - consumed time)
     */
    @ApiStatus.OverrideOnly
    int advance(int elapsedTime);

    boolean isAnimating();

    boolean isAnimatingReverse();

    default boolean isAnimatingForward() {
        return isAnimating() && !isAnimatingReverse();
    }

    static int getTimeDiff(long startTime) {
        return getTimeDiff(startTime, Minecraft.getSystemTime());
    }

    static int getTimeDiff(long startTime, long currentTime) {
        long elapsedTime = Math.abs(currentTime - startTime);
        return (int) Math.min(Integer.MAX_VALUE, elapsedTime);
    }
}
