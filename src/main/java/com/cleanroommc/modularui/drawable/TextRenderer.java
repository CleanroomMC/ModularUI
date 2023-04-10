package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.future.GlStateManager;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.sizer.Area;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

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
    protected boolean scrollOnOverflow = false;

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

    protected void drawMeasuredLines(List<Line> measuredLines) {
        float maxW = 0;
        int y0 = getStartY(measuredLines.size());
        for (Line measuredLine : measuredLines) {
            int x0 = getStartX(measuredLine.width);
            maxW = Math.max(draw(measuredLine.text, x0, y0), maxW);
            y0 += getFontHeight();
        }
        this.lastWidth = maxWidth > 0 ? Math.min(maxW, maxWidth) : maxW;
        this.lastHeight = measuredLines.size() * getFontHeight();
        this.lastWidth = Math.max(0, this.lastWidth - scale);
        this.lastHeight = Math.max(0, this.lastHeight - scale);
    }

    public void drawSimple(String text) {
        float w = getFontRenderer().getStringWidth(text) * scale;
        int y = getStartY(1), x = getStartX(w);
        draw(text, x, y);
        this.lastWidth = w;
        this.lastHeight = getFontHeight();
        this.lastWidth = Math.max(0, this.lastWidth - scale);
        this.lastHeight = Math.max(0, this.lastHeight - scale);
    }

    public List<Line> measureLines(List<String> lines) {
        List<Line> measuredLines = new ArrayList<>();
        for (String line : lines) {
            for (String subLine : wrapLine(line)) {
                measuredLines.add(line(subLine));
            }
        }
        return measuredLines;
    }

    public void drawCut(String text) {
        if (text.contains("\n")) {
            throw new IllegalArgumentException("Scrolling text can't wrap!");
        }
        drawCut(line(text));
    }

    public void drawCut(Line line) {
        if (line.width > this.maxWidth) {
            String cutText = getFontRenderer().trimStringToWidth(line.getText(), (int) (this.maxWidth - 6)) + "...";
            drawMeasuredLines(Collections.singletonList(line(cutText)));
        } else {
            drawMeasuredLines(Collections.singletonList(line));
        }
    }

    public void drawScrolling(Line line, int scroll, Area area, GuiContext context) {
        if (line.getWidth() <= this.maxWidth) {
            drawMeasuredLines(Collections.singletonList(line));
            return;
        }
        scroll = scroll % (int) (line.width + 1) ;
        String drawString = line.getText();//getFontRenderer().trimStringToWidth(line.getText(), (int) (this.maxWidth + scroll));
        Area.SHARED.set(this.x, Integer.MIN_VALUE, this.x + (int) this.maxWidth, Integer.MAX_VALUE);
        Scissor.scissor(Area.SHARED, context);
        GlStateManager.translate(-scroll, 0, 0);
        drawMeasuredLines(Collections.singletonList(line(drawString)));
        GlStateManager.translate(scroll, 0, 0);
        Scissor.unscissor(context);
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
        List<Line> measuredLines = measureLines(lines);
        float w = 0;
        for (Line measuredLine : measuredLines) {
            w = Math.max(w, measuredLine.getWidth());
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
        GL11.glDisable(GL11.GL_LIGHTING);
        int width = getFontRenderer().drawString(text, (int) (x / scale), (int) (y / scale), color, shadow);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
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

    public Line line(String text) {
        return new Line(text, getFontRenderer().getStringWidth(text) * this.scale);
    }

    public static class Line {

        private final String text;
        private final float width;

        public Line(String text, float width) {
            this.text = text;
            this.width = width;
        }

        public String getText() {
            return text;
        }

        public float getWidth() {
            return width;
        }

        public int upperWidth() {
            return (int) (width + 1);
        }

        public int lowerWidth() {
            return (int) (width + 1);
        }
    }
}
