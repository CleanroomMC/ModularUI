package com.cleanroommc.modularui.animation;

public interface IAnimatable<T extends IAnimatable<T>> {

    T interpolate(T start, T end, float t);

    T copyOrImmutable();

    default void animateTo(T target, boolean reverse) {
        T self = (T) this;
        new MutableObjectAnimator<>(self, target).animate(reverse);
    }
}
