package com.cleanroommc.modularui.utils;

import net.minecraft.client.Minecraft;

public class FpsCounter {

    private int fps = 0, frameCount = 0;
    private long timer = Minecraft.getSystemTime();

    public void reset() {
        this.fps = 0;
        this.frameCount = 0;
        this.timer = Minecraft.getSystemTime();
    }

    public void onDraw() {
        frameCount++;
        long time = Minecraft.getSystemTime();
        if (time - timer >= 1000) {
            fps = frameCount;
            frameCount = 0;
            timer += 1000;
        }
    }

    public int getFps() {
        return fps;
    }
}
