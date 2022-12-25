package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.IIcon;
import com.cleanroommc.modularui.utils.Alignment;

public class TextIcon implements IIcon {

    private final String text;
    private final int width, height;
    private int color = 0xFFFFFF;
    private boolean shadow;

    public TextIcon(String text, int width, int height) {
        this.text = text;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(int x, int y, int width, int height) {
        TextRenderer.SHARED.setAlignment(Alignment.TopLeft, width);
        TextRenderer.SHARED.setPos(x, y);
        TextRenderer.SHARED.setColor(0xFFFFFF);
        TextRenderer.SHARED.setShadow(false);
        TextRenderer.SHARED.drawSimple(text);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public String getText() {
        return text;
    }
}
