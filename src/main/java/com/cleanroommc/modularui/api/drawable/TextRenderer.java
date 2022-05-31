package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextRenderer {

    protected float maxWidth = -1, maxHeight = -1;
    protected int x = 0, y = 0;
    protected Alignment alignment = Alignment.TopLeft;
    protected float scale = 1f;
    protected boolean shadow = false;
    protected int color = Theme.INSTANCE.getText();
    protected boolean simulate;
    protected float lastWidth = 0, lastHeight = 0;

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

    public void setPos(int x, int y) {
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

    public void draw(String text) {
        draw(Collections.singletonList(text));
    }

    public void draw(List<String> lines) {
        drawMeasuredLines(measureLines(lines));
    }

    protected void drawMeasuredLines(List<Pair<String, Float>> measuredLines) {
        float maxW = 0;
        int y0 = getStartY(measuredLines.size());
        for (Pair<String, Float> measuredLine : measuredLines) {
            int x0 = getStartX(measuredLine.getRight());
            maxW = Math.max(draw(measuredLine.getLeft(), x0, y0), maxW);
            y0 += getFontRenderer().FONT_HEIGHT * scale;
        }
        this.lastWidth = maxWidth > 0 ? Math.min(maxW, maxWidth) : maxW;
        this.lastHeight = measuredLines.size() * getFontHeight();
        this.lastWidth = Math.max(0, this.lastWidth - 1);
        this.lastHeight = Math.max(0, this.lastHeight - 1);
    }

    public List<Pair<String, Float>> measureLines(List<String> lines) {
        List<Pair<String, Float>> measuredLines = new ArrayList<>();
        for (String line : lines) {
            for (String subLine : wrapLine(line)) {
                float width = getFontRenderer().getStringWidth(subLine) * scale;
                measuredLines.add(Pair.of(subLine, width));
            }
        }
        return measuredLines;
    }

    public List<String> wrapLine(String line) {
        return maxWidth > 0 ? getFontRenderer().listFormattedStringToWidth(line, (int) (maxWidth / scale)) : Collections.singletonList(line);
    }

    public boolean wouldFit(List<String> text) {
        if (maxHeight > 0 && maxHeight < text.size() * getFontHeight() - scale) {
            return false;
        }
        if (maxWidth > 0) {
            for (String line : text) {
                if (maxWidth < getFontRenderer().getStringWidth(line)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getMaxWidth(List<String> lines) {
        if (lines.isEmpty()) {
            return 0;
        }
        List<Pair<String, Float>> measuredLines = measureLines(lines);
        float w = 0;
        for (Pair<String, Float> measuredLine : measuredLines) {
            w = Math.max(w, measuredLine.getRight());
        }
        return (int) Math.ceil(w);
    }

    protected int getStartY(int lines) {
        if (alignment.y >= 0 && maxHeight > 0) {
            float height = lines * getFontHeight() - scale;
            if (alignment.y > 0) {
                return (int) (y + maxHeight - height);
            } else {
                return (int) (y + (maxHeight - height) / 2f);
            }
        }
        return y;
    }

    protected int getStartX(float lineWidth) {
        if (maxWidth > 0 && alignment.x >= 0) {
            if (alignment.x > 0) {
                return (int) (x + maxWidth - lineWidth);
            } else {
                return (int) (x + (maxWidth - lineWidth) / 2f);
            }
        }
        return x;
    }

    protected float draw(String text, float x, float y) {
        if (simulate) {
            return getFontRenderer().getStringWidth(text);
        }
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0f);
        int width = getFontRenderer().drawString(text, x / scale, y / scale, color, shadow);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        return width * scale;
    }

    public float getFontHeight() {
        return getFontRenderer().FONT_HEIGHT * scale;
    }

    public float getLastHeight() {
        return lastHeight;
    }

    public float getLastWidth() {
        return lastWidth;
    }

    public Size getLastSize() {
        return new Size(lastWidth, lastHeight);
    }

    @SideOnly(Side.CLIENT)
    public static FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }
}
