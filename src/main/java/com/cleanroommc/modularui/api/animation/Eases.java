package com.cleanroommc.modularui.api.animation;

public enum Eases implements IEase {
    EaseLinear(input -> input),
    EaseQuadIn(input -> input * input),
    EaseQuadInOut(input -> {
        if ((input /= 0.5f) < 1) {
            return 0.5f * input * input;
        }
        return -0.5f * ((--input) * (input - 2) - 1);
    }),
    EaseQuadOut(input -> -input * (input - 2));


    IEase ease;

    Eases(IEase ease) {
        this.ease = ease;
    }

    @Override
    public float interpolate(float t) {
        return ease.interpolate(t);
    }
}
