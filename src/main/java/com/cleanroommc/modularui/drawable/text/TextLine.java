package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.client.gui.FontRenderer;

public class TextLine implements ITextLine {

    private final String text;
    private final int width;

    private float lastX, lastY;

    public TextLine(String text, int width) {
        this.text = text;
        this.width = width;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight(FontRenderer fr) {
        return fr.FONT_HEIGHT + 1;
    }

    @Override
    public void draw(GuiContext context, FontRenderer fr, float x, float y, int color, boolean shadow, int availableWidth, int availableHeight) {
        Platform.setupDrawFont();
        fr.drawString(this.text, x, y, color, shadow);
        this.lastX = x;
        this.lastY = y;
    }

    @Override
    public Object getHoveringElement(FontRenderer fr, int x, int y) {
        if (y < lastY || y > lastY + getHeight(fr)) return null;
        if (x < lastX || x > lastX + getWidth()) return Boolean.FALSE; // not hovering, but we know that nothing else is hovered either
        return this.text;
    }

    @Override
    public String toString() {
        return text;
    }
}
