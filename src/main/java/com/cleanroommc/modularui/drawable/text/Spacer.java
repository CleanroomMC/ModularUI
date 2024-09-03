package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

import net.minecraft.client.gui.FontRenderer;

public class Spacer implements ITextLine {

    private final int space;

    public Spacer(int space) {
        this.space = space;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight(FontRenderer fr) {
        return this.space;
    }

    @Override
    public void draw(GuiContext context, FontRenderer fr, float x, float y, int color, boolean shadow) {}

    @Override
    public Object getHoveringElement(FontRenderer fr, int x, int y) {
        return null;
    }
}
