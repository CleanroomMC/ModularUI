package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.sizer.Box;

public class TextIcon implements IIcon {

    private final String text;
    private final int width, height;
    private static final Box margin = new Box();

    public TextIcon(String text, int width, int height) {
        this.text = text;
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height) {
        TextRenderer.SHARED.setPos(x, y);
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

    @Override
    public Box getMargin() {
        return margin;
    }

    public String getText() {
        return text;
    }
}
