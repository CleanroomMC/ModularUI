package com.cleanroommc.modularui.animation;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.Nullable;

public abstract class BaseAnimator implements IAnimator {

    private IAnimator parent;

    private long startTime = 0;
    private byte direction = 0;

    void setParent(IAnimator parent) {
        this.parent = parent;
    }

    @Nullable
    public final IAnimator getParent() {
        return parent;
    }

    @Override
    public void animate(boolean reverse) {
        restartAnimation(reverse);
        if (this.parent == null) AnimatorManager.startAnimation(this);
    }

    protected void restartAnimation(boolean reverse) {
        this.startTime = Minecraft.getSystemTime();
        this.direction = (byte) (reverse ? -1 : 1);
    }

    @Override
    public void stop() {
        this.direction = 0;
    }

    @Override
    public boolean isAnimating() {
        return this.direction != 0;
    }

    @Override
    public boolean isAnimatingReverse() {
        return this.direction < 0;
    }

    @Override
    public boolean isAnimatingForward() {
        return this.direction > 0;
    }

    public final byte getDirection() {
        return direction;
    }

    protected long getStartTime() {
        return startTime;
    }
}
