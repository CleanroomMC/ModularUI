package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

import net.minecraft.client.gui.FontRenderer;

public class Spacer implements ITextLine {

    public static final Spacer SPACER_2PX = new Spacer(2);
    public static final Spacer LINE_SPACER = new Spacer(FontRenderHelper.getDefaultTextHeight());

    public static Spacer of(int space) {
        if (space == 2) return SPACER_2PX;
        if (space == LINE_SPACER.space) return LINE_SPACER;
        return new Spacer(space);
    }

    private final int space;

    protected Spacer(int space) {
        this.space = space;
    }

    public int getSpace() {
        return space;
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
    public void draw(GuiContext context, FontRenderer fr, float x, float y, int color, boolean shadow, int availableWidth,
                     int availableHeight) {}

    @Override
    public Object getHoveringElement(FontRenderer fr, int x, int y) {
        return null;
    }
}
