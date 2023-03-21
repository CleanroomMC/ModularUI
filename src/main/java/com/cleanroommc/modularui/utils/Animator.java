package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.drawable.IInterpolation;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

public class Animator {

    private static final List<Animator> activeAnimators = new ArrayList<>();

    @ApiStatus.Internal
    public static void advance() {
        activeAnimators.removeIf(Animator::tick);
    }

    private final int duration;
    private int progress;
    private int dir = 0;
    private double min = 0, max = 1;
    private double value;
    private final IInterpolation interpolation;
    private DoubleConsumer callback;
    private DoubleConsumer endCallback;

    public Animator(int duration, IInterpolation interpolation) {
        this.duration = duration;
        this.interpolation = interpolation;
    }

    public Animator setCallback(DoubleConsumer callback) {
        this.callback = callback;
        return this;
    }

    public Animator setEndCallback(DoubleConsumer endCallback) {
        this.endCallback = endCallback;
        return this;
    }

    public Animator setValueBounds(double min, double max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public void forward() {
        this.progress = 0;
        this.dir = 1;
        updateValue();
        if (!activeAnimators.contains(this)) {
            activeAnimators.add(this);
        }
    }

    public void backward() {
        this.progress = duration;
        this.dir = -1;
        updateValue();
        if (!activeAnimators.contains(this)) {
            activeAnimators.add(this);
        }
    }

    public boolean isRunning() {
        return dir != 0 && dir > 0 ? progress < duration : progress > 0;
    }

    public double getValue() {
        return value;
    }

    public int getDuration() {
        return duration;
    }

    public int getProgress() {
        return progress;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    private boolean tick() {
        this.progress += dir;
        updateValue();
        if (callback != null) callback.accept(value);
        if (!isRunning()) {
            if (endCallback != null) endCallback.accept(value);
            return true;
        }
        return false;
    }

    private void updateValue() {
        this.value = interpolation.interpolate(min, max, progress / (double) duration);
    }
}
