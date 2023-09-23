package com.cleanroommc.modularui.api.drawable;

import net.minecraft.util.IStringSerializable;

/**
 * A function which interpolates between two values.
 */
public interface IInterpolation extends IStringSerializable {

    float interpolate(float a, float b, float x);

}
