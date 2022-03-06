package com.cleanroommc.modularui.api.animation;

@FunctionalInterface
public interface IEase {
    float interpolate(float t);
}
