package com.cleanroommc.modularui.animation;

import java.util.ArrayList;
import java.util.List;

public class SequentialAnimator extends BaseAnimator implements IAnimator {

    private final List<IAnimator> animators;
    private int currentIndex = 0;

    public SequentialAnimator(List<IAnimator> animators) {
        this.animators = new ArrayList<>(animators);
        this.animators.forEach(animator -> {
            if (animator instanceof BaseAnimator baseAnimator) {
                baseAnimator.setParent(this);
            }
        });
    }

    @Override
    public void animate(boolean reverse) {
        if (this.animators.isEmpty()) return;
        super.animate(reverse);
        // start first animation
        this.animators.get(this.currentIndex).animate(reverse);
    }

    @Override
    public void reset(boolean atEnd) {
        this.currentIndex = atEnd ? this.animators.size() - 1 : 0;
        this.animators.forEach(animator -> animator.reset(atEnd));
    }

    @Override
    public int advance(int elapsedTime) {
        while (isAnimating() && elapsedTime > 0) {
            IAnimator animator = this.animators.get(currentIndex);
            elapsedTime -= animator.advance(elapsedTime);
            if (!animator.isAnimating()) {
                // animator has finished
                this.currentIndex += getDirection();
                if (this.currentIndex >= this.animators.size() || this.currentIndex < 0) {
                    // whole sequence has finished
                    stop();
                } else {
                    // start next animation
                    animator = this.animators.get(this.currentIndex);
                    animator.animate(isAnimatingReverse());
                }
            }
        }
        return elapsedTime;
    }
}
