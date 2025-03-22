package com.cleanroommc.modularui.animation;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class AnimatorManager {

    private static final List<IAnimator> animators = new ArrayList<>(16);
    private static final List<IAnimator> queuedAnimators = new ArrayList<>(8);
    private static long lastTime = 0;

    static void startAnimation(IAnimator animator) {
        if (!animators.contains(animator) && !queuedAnimators.contains(animator)) {
            queuedAnimators.add(animator);
        }
    }

    private AnimatorManager() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new AnimatorManager());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        long time = Minecraft.getSystemTime();
        int elapsedTime = IAnimator.getTimeDiff(lastTime, time);
        if (lastTime > 0 && !animators.isEmpty()) {
            animators.removeIf(animator -> {
                if (animator == null) return true;
                animator.advance(elapsedTime);
                return !animator.isAnimating();
            });
        }
        lastTime = time;
        animators.addAll(queuedAnimators);
        queuedAnimators.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDraw(GuiOpenEvent event) {
        if (event.getGui() == null) {
            // stop and yeet all animators on gui close
            animators.forEach(IAnimator::stop);
            animators.clear();
        }
    }
}
