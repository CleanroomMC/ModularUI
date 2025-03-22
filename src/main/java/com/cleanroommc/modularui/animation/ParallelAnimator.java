package com.cleanroommc.modularui.animation;

import java.util.ArrayList;
import java.util.List;

public class ParallelAnimator extends BaseAnimator implements IAnimator {

    private final List<IAnimator> animators;
    private int finishedAnimating = 0;

    public ParallelAnimator(List<IAnimator> animators) {
        this.animators = new ArrayList<>(animators);
        this.animators.forEach(animator -> {
            if (animator instanceof BaseAnimator baseAnimator) {
                baseAnimator.setParent(this);
            }
        });
    }

    @Override
    public void animate(boolean reverse) {
        super.animate(reverse);
        for (IAnimator animator : animators) {
            animator.animate(reverse);
        }
    }

    @Override
    public void stop() {
        super.stop();
        for (IAnimator animator : animators) {
            animator.stop();
        }
    }

    @Override
    public void reset(boolean atEnd) {
        this.finishedAnimating = 0;
        for (IAnimator animator : animators) {
            animator.reset(atEnd);
        }
    }

    @Override
    public int advance(int elapsedTime) {
        int remainingTime = 0;
        for (IAnimator animator : animators) {
            if (!animator.isAnimating()) continue;
            remainingTime = Math.max(remainingTime, animator.advance(elapsedTime));
            if (!animator.isAnimating()) {
                this.finishedAnimating++;
                if (isFinished()) {
                    stop();
                    return remainingTime;
                }
            }
        }
        return remainingTime;
    }

    public boolean isFinished() {
        return this.finishedAnimating == this.animators.size();
    }
}
