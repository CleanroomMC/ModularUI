package com.cleanroommc.modularui.drawable.graph;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface MajorTickFinder {

    float[] find(float min, float max, float[] ticks);
}
