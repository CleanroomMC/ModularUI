package com.cleanroommc.modularui.drawable.graph;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class AutoMajorTickFinder implements MajorTickFinder {

    private final boolean autoAdjust;
    private float multiple = 10;

    public AutoMajorTickFinder(boolean autoAdjust) {
        this.autoAdjust = autoAdjust;
    }

    public AutoMajorTickFinder(float multiple) {
        this.autoAdjust = false;
        this.multiple = multiple;
    }

    @Override
    public float[] find(float min, float max, float[] ticks) {
        int s = (int) Math.ceil((max - min) / multiple) + 2;
        if (s > ticks.length) ticks = new float[s];
        float next = (float) (Math.floor(min / multiple) * multiple);
        for (int i = 0; i < s; i++) {
            ticks[i] = next;
            if (next > max) {
                s = i + 1;
                break;
            }
            next += multiple;
        }
        if (ticks.length > s) ticks[s] = Float.NaN;
        return ticks;
    }

    void calculateAutoTickMultiple(float min, float max) {
        float step = (max - min) / 5;
        if (step < 1) {
            int significantPlaces = (int) Math.abs(Math.log10(step)) + 2;
            float ten = (float) Math.pow(10, significantPlaces);
            step = (int) (step * ten + 0.2f) / ten;
        } else if (step == 1) {
            step = 0.2f;
        } else {
            int significantPlaces = (int) Math.log10(step) - 1;
            float ten = (float) Math.pow(10, significantPlaces);
            step = (int) (step / ten + 0.2f) * ten;
        }
        setMultiple(step);
    }

    public boolean isAutoAdjust() {
        return autoAdjust;
    }

    public void setMultiple(float multiple) {
        this.multiple = multiple;
    }
}
