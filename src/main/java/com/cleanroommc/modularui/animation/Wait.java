package com.cleanroommc.modularui.animation;

public class Wait extends BaseAnimator {

    private int duration;
    private int progress = 0;

    public Wait() {
        this(250);
    }

    public Wait(int duration) {
        this.duration = duration;
    }

    @Override
    public void reset(boolean atEnd) {
        this.progress = 0;
    }

    @Override
    public int advance(int elapsedTime) {
        int max = this.duration - this.progress;
        int prog = Math.min(max, elapsedTime);
        this.progress += prog;
        if (this.progress >= this.duration) {
            stop(false);
        }
        return elapsedTime - prog;
    }

    @Override
    public boolean hasProgressed() {
        return progress > 0 && isAnimating();
    }

    public Wait duration(int duration) {
        this.duration = duration;
        return this;
    }
}
