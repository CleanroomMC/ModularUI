package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AnimatedText extends StyledText {

    private String fullString;
    private String currentString = "";
    private int currentIndex;
    private int speed = 40; // ms per char
    private long timeLastDraw;
    private boolean forward = true;

    private boolean isAnimating = false;

    public AnimatedText(IKey key) {
        super(key);
    }

    @Override
    public String get() {
        return this.currentString;
    }

    public void reset() {
        this.fullString = null;
    }

    private void advance() {
        if (!this.isAnimating || (this.forward && this.currentIndex >= this.fullString.length()) || (!this.forward && this.currentIndex < 0))
            return;
        long time = Minecraft.getSystemTime();
        int amount = (int) ((time - this.timeLastDraw) / this.speed);
        if (amount == 0) return;
        if (this.forward) {
            int max = Math.min(this.fullString.length() - 1, this.currentIndex + amount);
            this.currentIndex = Math.max(this.currentIndex, 0);
            for (int i = this.currentIndex; i < max; i++) {
                char c = this.fullString.charAt(i);
                if (c == ' ') {
                    max = Math.min(this.fullString.length() - 1, max + 1);
                }
                //noinspection StringConcatenationInLoop
                this.currentString += c;
            }
            this.currentIndex = max;
        } else {
            int min = Math.max(0, this.currentIndex - amount);
            this.currentIndex = Math.min(this.currentIndex, this.currentString.length() - 1);
            for (int i = this.currentIndex; i >= min; i--) {
                char c = this.fullString.charAt(i);
                if (c == ' ') {
                    min = Math.max(0, min - 1);
                }
                this.currentString = this.currentString.substring(0, i);
            }
            this.currentIndex = min;
        }
        this.timeLastDraw += (long) amount * this.speed;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height) {
        if (this.fullString == null || !this.fullString.equals(super.get())) {
            if (this.isAnimating) {
                this.fullString = super.get();
                this.currentString = this.forward ? "" : this.fullString;
                this.currentIndex = this.forward ? 0 : this.fullString.length() - 1;
                this.timeLastDraw = Minecraft.getSystemTime();
            } else {
                this.currentString = this.forward ? "" : this.fullString;
            }
        }
        advance();
        if (this.currentString.isEmpty()) return;
        super.draw(context, x, y, width, height);
    }

    public AnimatedText startAnimation() {
        this.isAnimating = true;
        return this;
    }

    public AnimatedText stopAnimation() {
        this.isAnimating = false;
        return this;
    }

    public AnimatedText forward(boolean forward) {
        this.forward = forward;
        return this;
    }

    @Override
    public AnimatedText alignment(Alignment alignment) {
        return (AnimatedText) super.alignment(alignment);
    }

    @Override
    public AnimatedText color(int color) {
        return (AnimatedText) super.color(color);
    }

    @Override
    public AnimatedText scale(float scale) {
        return (AnimatedText) super.scale(scale);
    }

    @Override
    public AnimatedText shadow(boolean shadow) {
        return (AnimatedText) super.shadow(shadow);
    }

    /**
     * How fast the characters appear
     *
     * @param speed in ms per character
     */
    public AnimatedText speed(int speed) {
        this.speed = speed;
        return this;
    }
}
