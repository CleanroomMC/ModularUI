package com.cleanroommc.modularui.api.animation;

import java.util.function.Consumer;

public class Interpolator {

    private final float from;
    private final float to;
    private final int duration;
    private final IEase ease;
    private final Consumer<Number> interpolate;
    private final Consumer<Number> callback;

    private int runs;
    private int progress = 0;

    public Interpolator(float from, float to, int duration, IEase ease, Consumer<Number> interpolate) {
        this(from, to, duration, ease, interpolate, null);
    }

    public Interpolator(float from, float to, int duration, IEase ease, Consumer<Number> interpolate, Consumer<Number> callback) {
        this.from = from;
        this.to = to;
        this.duration = duration;
        this.ease = ease;
        this.interpolate = interpolate;
        this.callback = callback;
    }

    public void stop() {
        runs = 0;
    }

    public Interpolator forward() {
        progress = 0;
        runs = 1;
        return this;
    }

    public void backwards() {
        progress = duration;
        runs = -1;
    }

    public boolean isAtStart() {
        return progress == 0;
    }

    public boolean isAtEnd() {
        return progress >= duration;
    }

    public void update(float partialTicks) {
        if (runs != 0) {
            if (runs == -1 && progress <= 0) {
                progress = 0;
                if (callback != null) {
                    callback.accept(ease.interpolate(progress * 1.0f / duration) * (to - from) + from);
                }
                stop();
                return;
            } else if (runs == 1 && progress >= duration) {
                progress = duration;
                if (callback != null) {
                    callback.accept(ease.interpolate(progress * 1.0f / duration) * (to - from) + from);
                }
                stop();
                return;
            } else {
                interpolate.accept(ease.interpolate(progress * 1.0f / duration) * (to - from) + from);
            }
            progress += partialTicks * 50 * runs;
        }
    }
}
