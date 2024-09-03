package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

import net.minecraft.client.gui.FontRenderer;

import java.util.List;

public class ComposedLine implements ITextLine {

    private final List<Object> elements;
    private final int width;
    private final int height;

    public ComposedLine(List<Object> elements, int width, int height) {
        this.elements = elements;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight(FontRenderer fr) {
        return height;
    }

    @Override
    public void draw(GuiContext context, FontRenderer fr, float x, float y, int color, boolean shadow) {
        for (Object o : this.elements) {
            if (o instanceof String s) {
                float drawY = getHeight(fr) / 2f - fr.FONT_HEIGHT / 2f;
                fr.drawString(s, x, (int) (y + drawY), color, shadow);
                x += fr.getStringWidth(s);
            } else if (o instanceof IIcon icon) {
                float drawY = getHeight(fr) / 2f - icon.getHeight() / 2f;
                icon.draw(context, (int) x, (int) (y + drawY), icon.getWidth(), icon.getHeight(), IThemeApi.get().getDefaultTheme().getFallback());
                x += icon.getWidth();
            }
        }
    }
}
