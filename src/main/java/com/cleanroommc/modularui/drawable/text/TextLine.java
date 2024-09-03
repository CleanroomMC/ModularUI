package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import net.minecraft.client.gui.FontRenderer;

public class TextLine implements ITextLine {

    private final String text;
    private final int width;

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
        return fr.FONT_HEIGHT;
    }

    @Override
    public void draw(GuiContext context, FontRenderer fr, float x, float y, int color, boolean shadow) {
        fr.drawString(this.text, x, y, color, shadow);
    }
}
