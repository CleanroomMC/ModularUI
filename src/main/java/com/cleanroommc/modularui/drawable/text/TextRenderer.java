package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    protected float lastActualWidth = 0;
    protected float lastTrimmedWidth = 0;
    protected float lastActualHeight = 0;
    protected float lastTrimmedHeight = 0;
    protected float lastX = 0, lastY = 0;
    protected boolean hardWrapOnBorder = true;
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

    public void setHardWrapOnBorder(boolean hardWrapOnBorder) {
        this.hardWrapOnBorder = hardWrapOnBorder;
    }

    public void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public void draw(String text) {
        if ((this.maxWidth <= 0 || !this.hardWrapOnBorder) && !text.contains("\n'")) {
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
        int y0 = getStartYOfLines(measuredLines.size());
        for (Line measuredLine : measuredLines) {
            int x0 = getStartX(measuredLine.width);
            maxW = Math.max(maxW, measuredLine.width);
            draw(measuredLine.text, x0, y0);
            y0 += (int) getFontHeight();
        }
        this.lastActualWidth = this.maxWidth > 0 ? Math.min(maxW, this.maxWidth) : maxW;
        this.lastActualHeight = measuredLines.size() * getFontHeight();
        this.lastTrimmedWidth = Math.max(0, this.lastActualWidth - this.scale);
        this.lastTrimmedHeight = Math.max(0, this.lastActualHeight - this.scale);
    }

    public void drawSimple(String text) {
        float w = getFontRenderer().getStringWidth(text) * this.scale;
        int y = getStartYOfLines(1), x = getStartX(w);
        draw(text, x, y);
        this.lastActualWidth = w;
        this.lastActualHeight = getFontHeight();
        this.lastTrimmedWidth = Math.max(0, this.lastActualWidth - this.scale);
        this.lastTrimmedHeight = Math.max(0, this.lastActualHeight - this.scale);
    }

    public List<Line> measureLines(List<String> lines) {
        List<Line> measuredLines = new ArrayList<>();
        for (String line : lines) {
            if (this.hardWrapOnBorder) {
                for (String subLine : wrapLine(line)) {
                    measuredLines.add(line(subLine));
                }
            } else {
                measuredLines.add(line(line));
            }
        }
        return measuredLines;
    }

    public List<ITextLine> compile(List<Object> rawText) {
        return RichTextCompiler.INSTANCE.compileLines(getFontRenderer(), rawText, (int) this.maxWidth, this.scale);
    }

    public List<ITextLine> compileAndDraw(GuiContext context, List<Object> raw) {
        List<ITextLine> lines = compile(raw);
        drawCompiled(context, lines);
        return lines;
    }

    public void drawCompiled(GuiContext context, List<ITextLine> lines) {
        int height = 0, width = 0;
        for (ITextLine line : lines) {
            height += line.getHeight(getFontRenderer());
            width = Math.max(width, line.getWidth());
        }
        if (!this.simulate) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.x, this.y, 10);
            GlStateManager.scale(this.scale, this.scale, 1f);
            GlStateManager.translate(-this.x, -this.y, 0);
        }
        int y0 = getStartY(height, height);
        this.lastY = y0;
        for (ITextLine line : lines) {
            int x0 = getStartX(width, line.getWidth());
            if (!simulate) line.draw(context, getFontRenderer(), x0, y0, this.color, this.shadow, width, height);
            y0 += line.getHeight(getFontRenderer());
        }
        if (!this.simulate) GlStateManager.popMatrix();
        this.lastActualWidth = this.maxWidth > 0 ? Math.min(width * this.scale, this.maxWidth) : width * this.scale;
        this.lastActualHeight = height * this.scale;
        this.lastTrimmedWidth = Math.max(0, this.lastActualWidth - this.scale);
        this.lastTrimmedHeight = Math.max(0, this.lastActualHeight - this.scale);
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

    public void drawScrolling(Line line, float progress, Area area, GuiContext context) {
        if (line.getWidth() <= this.maxWidth) {
            drawMeasuredLines(Collections.singletonList(line));
            return;
        }
        float scroll = (this.maxWidth - line.getWidth()) * progress;
        //scroll = scroll % (int) (line.width + 1);
        String drawString = line.getText();//getFontRenderer().trimStringToWidth(line.getText(), (int) (this.maxWidth + scroll));
        Stencil.apply(this.x, -500, (int) this.maxWidth, 1000, context);
        GlStateManager.translate(scroll, 0, 0);
        drawMeasuredLines(Collections.singletonList(line(drawString)));
        GlStateManager.translate(-scroll, 0, 0);
        Stencil.remove();
    }

    public List<String> wrapLine(String line) {
        return this.maxWidth > 0 ? getFontRenderer().listFormattedStringToWidth(line, (int) (this.maxWidth / this.scale)) : Collections.singletonList(line);
    }

    public boolean wouldFit(List<String> text) {
        if (this.maxHeight > 0 && this.maxHeight < text.size() * getFontHeight() - this.scale) {
            return false;
        }
        if (this.maxWidth > 0) {
            for (String line : text) {
                if (this.maxWidth < getFontRenderer().getStringWidth(line)) {
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

    protected int getStartYOfLines(int lines) {
        return getStartY(lines * getFontHeight() - this.scale);
    }

    protected int getStartY(float height) {
        return getStartY(this.maxHeight, height);
    }

    protected int getStartY(float maxHeight, float height) {
        if (this.alignment.y > 0 && maxHeight > 0 && height != maxHeight) {
            return (int) (this.y + (maxHeight * this.alignment.y) - height * this.alignment.y);
        }
        return this.y;
    }

    protected int getStartX(float lineWidth) {
        return getStartX(this.maxWidth, lineWidth);
    }

    protected int getStartX(float maxWidth, float lineWidth) {
        if (this.alignment.x > 0 && maxWidth > 0) {
            return (int) (this.x + (maxWidth * this.alignment.x) - lineWidth * this.alignment.x);
        }
        return this.x;
    }

    protected void draw(String text, float x, float y) {
        if (this.simulate) return;
        Platform.setupDrawFont();
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.scale, this.scale, 0f);
        getFontRenderer().drawString(text, x / this.scale, y / this.scale, this.color, this.shadow);
        GlStateManager.popMatrix();
    }

    public int getColor() {
        return color;
    }

    public float getScale() {
        return scale;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getFontHeight() {
        return getFontRenderer().FONT_HEIGHT * this.scale;
    }

    public float getLastActualHeight() {
        return this.lastActualHeight;
    }

    public float getLastActualWidth() {
        return this.lastActualWidth;
    }

    public float getLastTrimmedWidth() {
        return lastTrimmedWidth;
    }

    public float getLastTrimmedHeight() {
        return lastTrimmedHeight;
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
            return this.text;
        }

        public float getWidth() {
            return this.width;
        }

        public int upperWidth() {
            return (int) (this.width + 1);
        }

        public int lowerWidth() {
            return (int) this.width;
        }
    }
}
