package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TextRenderer {

    public static final FontRenderer FR = Minecraft.getMinecraft().fontRenderer;
    private float maxWidth = -1, maxHeight = -1;
    private float x = 0, y = 0;
    private Alignment alignment = Alignment.TopLeft;
    private float scale = 1f;
    private boolean shadow = false;
    private int color = 0x404040;
    private boolean simulate;

    public void setAlignment(Alignment alignment, float maxWidth) {
        setAlignment(alignment, maxWidth, -1);
    }

    public void setAlignment(Alignment alignment, float maxWidth, float maxHeight) {
        this.alignment = alignment;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setPos(Pos2d pos) {
        setPos(pos.x, pos.y);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public Size draw(String text) {
        return draw(Collections.singletonList(text));
    }

    public Size draw(List<String> lines) {
        if ((alignment.x >= 0 && maxWidth > 0) || (alignment.y >= 0 && maxHeight > 0)) {
            return drawAligned(lines);
        }
        float y0 = y;
        float maxW = 0;
        for (String line : lines) {
            List<String> subLines = maxWidth > 0 ? FR.listFormattedStringToWidth(line, (int) (maxWidth / scale)) : Arrays.asList(line.split("\n"));
            for (String subLine : subLines) {
                maxW = Math.max(draw(subLine, x, y0), maxW);
                y0 += FR.FONT_HEIGHT * scale;
            }
        }
        return new Size(maxWidth > 0 ? Math.min(maxW, maxWidth) : maxW, y0 - y);
    }

    public Size drawAligned(List<String> lines) {
        float maxW = 0;
        List<Pair<String, Float>> measuredLines = new ArrayList<>();
        for (String line : lines) {
            List<String> subLines = maxWidth > 0 ? FR.listFormattedStringToWidth(line, (int) (maxWidth / scale)) : Arrays.asList(line.split("\n"));
            for (String subLine : subLines) {
                float width = FR.getStringWidth(subLine) * scale;
                measuredLines.add(Pair.of(subLine, width));
                maxW = Math.max(width, maxW);
            }
        }
        float y0 = y;
        if (alignment.y >= 0 && maxHeight > 0) {
            float height = measuredLines.size() * FR.FONT_HEIGHT * scale;
            if (alignment.y > 0) {
                y0 = y + maxHeight - height;
            } else {
                y0 = y + (maxHeight - height) / 2f;
            }
        }
        for (Pair<String, Float> measuredLine : measuredLines) {
            float x0 = x;
            if (maxWidth > 0 && alignment.x >= 0) {
                if (alignment.x > 0) {
                    x0 = x + maxWidth - measuredLine.getValue();
                } else {
                    x0 = x + (maxWidth - measuredLine.getValue()) / 2f;
                }
            }
            draw(measuredLine.getKey(), x0, y0);
            y0 += FR.FONT_HEIGHT * scale;
        }
        return new Size(maxWidth > 0 ? Math.min(maxW, maxWidth) : maxW, measuredLines.size() * FR.FONT_HEIGHT * scale);
    }

    private float draw(String text, float x, float y) {
        if (simulate) {
            return FR.getStringWidth(text);
        }
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0f);
        int width = FR.drawString(text, x / scale, y / scale, color, shadow);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        return width * scale;
    }
}
