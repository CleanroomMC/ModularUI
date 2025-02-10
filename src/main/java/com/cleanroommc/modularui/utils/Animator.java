package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.api.drawable.IInterpolation;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;

public class Animator {

    private static final List<Animator> activeAnimators = new ArrayList<>();

    @ApiStatus.Internal
    public static void advance() {
        activeAnimators.removeIf(Animator::tick);
    }

    private final int duration;
    private int progress;
    private int dir = 0;
    private float min = 0, max = 1;
    private float value;
    private final IInterpolation interpolation;
    private DoublePredicate callback;
    private DoubleConsumer endCallback;

    public Animator(int duration, IInterpolation interpolation) {
        this.duration = duration;
        this.interpolation = interpolation;
    }

    public Animator setCallback(DoubleConsumer callback) {
        this.callback = val -> {
            callback.accept(val);
            return false;
        };
        return this;
    }

    public Animator setCallback(DoublePredicate callback) {
        this.callback = callback;
        return this;
    }

    public Animator setEndCallback(DoubleConsumer endCallback) {
        this.endCallback = endCallback;
        return this;
    }

    public Animator setValueBounds(float min, float max) {
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
        this.progress = this.duration;
        this.dir = -1;
        updateValue();
        if (!activeAnimators.contains(this)) {
            activeAnimators.add(this);
        }
    }

    public boolean isRunning() {
        return this.dir != 0 && (this.dir > 0 ? this.progress < this.duration : this.progress > 0);
    }

    public boolean isRunningForwards() {
        return this.dir > 0 && this.progress < this.duration;
    }

    public boolean isRunningBackwards() {
        return this.dir < 0 && this.progress > 0;
    }

    public double getValue() {
        return this.value;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getProgress() {
        return this.progress;
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    private boolean tick() {
        this.progress += this.dir;
        updateValue();
        if (this.callback != null && this.callback.test(this.value)) {
            this.dir = 0; // stop animation
        }
        if (!isRunning()) {
            if (this.endCallback != null) this.endCallback.accept(this.value);
            return true;
        }
        return false;
    }

    private void updateValue() {
        this.value = this.interpolation.interpolate(this.min, this.max, this.progress / (float) this.duration);
    }
}
