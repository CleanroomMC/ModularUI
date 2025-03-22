package com.cleanroommc.modularui.animation;

import com.cleanroommc.modularui.api.drawable.IInterpolation;
import com.cleanroommc.modularui.utils.Interpolation;

public class MutableObjectAnimator<T extends IAnimatable<T>> extends Animator {

    private final T from;
    private final T to;
    private final T animatable;

    private int duration = 250;
    private IInterpolation curve = Interpolation.LINEAR;

    private int progress = 0;

    public MutableObjectAnimator(T animatable, T to) {
        this.from = animatable.copyOrImmutable();
        this.to = to;
        this.animatable = animatable;
        bounds(0f, 1f);
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
        this.animatable.interpolate(this.from, this.to, getRawValue());
    }
}
