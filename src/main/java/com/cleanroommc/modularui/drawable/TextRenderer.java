package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.utils.Alignment;
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

    public static final TextRenderer SHARED = new TextRenderer();

    protected float maxWidth = -1, maxHeight = -1;
    protected int x = 0, y = 0;
    protected Alignment alignment = Alignment.TopLeft;
    protected float scale = 1f;
    protected boolean shadow = false;
    protected int color = 0;//Theme.INSTANCE.getText();
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

    public void setColor(int color) {
        this.color = color;
    }

    public void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public void draw(String text) {
        if (this.maxWidth <= 0 && !text.contains("\n'")) {
            drawSimple(text);
        } else {
            draw(Collections.singletonList(text));
        }
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
        this.lastWidth = Math.max(0, this.lastWidth - scale);
        this.lastHeight = Math.max(0, this.lastHeight - scale);
    }

    public void drawSimple(String text) {
        float w = getFontRenderer().getStringWidth(text);
        int y = getStartY(1), x = getStartX(w);
        draw(text, x, y);
        this.lastWidth = w;
        this.lastHeight = getFontHeight();
        this.lastWidth = Math.max(0, this.lastWidth - scale);
        this.lastHeight = Math.max(0, this.lastHeight - scale);
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
        if (alignment.y > 0 && maxHeight > 0) {
            float height = lines * getFontHeight() - scale;
            return (int) (y + (maxHeight * alignment.y) - height * alignment.y);
        }
        return y;
    }

    protected int getStartX(float lineWidth) {
        if (alignment.x > 0 && maxWidth > 0) {
            return (int) (x + (maxWidth * alignment.x) - lineWidth * alignment.x);
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

    @SideOnly(Side.CLIENT)
    public static FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }
}
