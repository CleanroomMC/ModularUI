package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.screen.viewport.GuiContext;

import net.minecraft.client.gui.FontRenderer;

public interface ITextLine {

    int getWidth();

    int getHeight(FontRenderer fr);

    void draw(GuiContext context, FontRenderer fr, float x, float y, int color, boolean shadow);

    Object getHoveringElement(FontRenderer fr, int x, int y);

}
