package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IHoverable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.client.gui.FontRenderer;

import java.util.List;

public class ComposedLine implements ITextLine {

    private final List<Object> elements;
    private final int width;
    private final int height;

    private float lastX, lastY;

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
        return height == fr.FONT_HEIGHT ? height : height + 1;
    }

    @Override
    public void draw(GuiContext context, FontRenderer fr, float x, float y, int color, boolean shadow, int availableWidth, int availableHeight) {
        this.lastX = x;
        this.lastY = y;
        for (Object o : this.elements) {
            if (o instanceof String s) {
                float drawY = getHeight(fr) / 2f - fr.FONT_HEIGHT / 2f;
                Platform.setupDrawFont();
                fr.drawString(s, x, (int) (y + drawY), color, shadow);
                x += fr.getStringWidth(s);
            } else if (o instanceof IIcon icon) {
                float drawY = getHeight(fr) / 2f - icon.getHeight() / 2f;
                int w = icon.getWidth() > 0  ? icon.getWidth() : availableWidth;
                icon.draw(context, (int) x, (int) (y + drawY), w, icon.getHeight(), IThemeApi.get().getDefaultTheme().getFallback());
                if (icon instanceof IHoverable hoverable) {
                    hoverable.setRenderedAt((int) x, (int) (y + drawY));
                }
                x += w;
            }
        }
    }

    @Override
    public Object getHoveringElement(FontRenderer fr, int x, int y) {
        int h0 = getHeight(fr);
        if (y < lastY || y > lastY + h0) return null; // is not hovering vertically
        if (x < lastX || x > lastX + getWidth()) return Boolean.FALSE; // is not hovering horizontally
        float x0 = x - this.lastX; // origin to 0
        float x1 = 0;
        float y0 = y - this.lastY; // origin to 0
        for (Object o : this.elements) {
            float w, h;
            if (o instanceof String s) {
                w = fr.getStringWidth(s);
                h = fr.FONT_HEIGHT;
            } else if (o instanceof IIcon icon) {
                w = icon.getWidth();
                h = icon.getWidth();
            } else continue;
            if (x0 > x1 && x0 < x1 + w) {
                // is inside horizontally
                if (h < h0) {
                    // is smaller than line height
                    int lower = (int) (h0 / 2f - h / 2);
                    int upper = (int) (h0 / 2f + h / 2 - 1f);
                    if (y0 < lower || y0 > upper) return Boolean.FALSE; // is outside vertically
                }
                return o; // found hovering
            }
            x1 += w; // move to next element
        }
        return null;
    }

    @Override
    public String toString() {
        if (this.elements.isEmpty()) return "[empty]";
        StringBuilder builder = new StringBuilder("[");
        for (Object o : this.elements) {
            if (o instanceof String s) {
                builder.append('"').append(s).append('"').append(", ");
            } else {
                builder.append(o).append(", ");
            }
        }
        builder.delete(builder.length() - 2, builder.length());
        return builder.append("]").toString();
    }
}
