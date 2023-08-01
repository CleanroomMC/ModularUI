package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.utils.Color;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

public class TextFieldRenderer extends TextRenderer {

    protected final TextFieldHandler handler;
    protected int markedColor = 0x2F72A8;
    protected boolean renderCursor = false;

    public TextFieldRenderer(TextFieldHandler handler) {
        this.handler = handler;
    }

    public void toggleCursor() {
        this.renderCursor = !this.renderCursor;
    }

    public void setCursor(boolean active) {
        this.renderCursor = active;
    }

    public void setMarkedColor(int markedColor) {
        this.markedColor = markedColor;
    }

    @Override
    protected void drawMeasuredLines(List<Line> measuredLines) {
        drawCursors(measuredLines);
        super.drawMeasuredLines(measuredLines);
    }

    @Override
    public List<String> wrapLine(String line) {
        return Collections.singletonList(line);
    }

    protected void drawCursors(List<Line> measuredLines) {
        if (!this.simulate) {
            Point2D.Float start;
            if (this.handler.hasTextMarked()) {
                start = getPosOf(measuredLines, this.handler.getStartCursor());
                // render Marked
                Point2D.Float end = getPosOf(measuredLines, this.handler.getEndCursor());

                if (start.y == end.y) {
                    drawMarked(start.y, start.x, end.x);
                } else {
                    int min = this.handler.getStartCursor().y;
                    int max = this.handler.getEndCursor().y;
                    Line line = measuredLines.get(min);
                    int startX = getStartX(line.getWidth());
                    drawMarked(start.y, start.x, startX + line.getWidth());
                    start.y += getFontHeight();
                    if (max - min > 1) {
                        for (int i = min + 1; i < max; i++) {
                            line = measuredLines.get(i);
                            startX = getStartX(line.getWidth());
                            drawMarked(start.y, startX, startX + line.getWidth());
                            start.y += getFontHeight();
                        }
                    }
                    line = measuredLines.get(max);
                    startX = getStartX(line.getWidth());
                    drawMarked(start.y, startX, end.x);
                }
            }
            // draw cursor
            Point main = this.handler.getMainCursor();
            start = getPosOf(measuredLines, main);
            if (this.renderCursor) {
                if (this.handler.getText().get(main.y).isEmpty()) {
                    start.x += 0.7f;
                }
                drawCursor(start.x, start.y);
            }
        }
    }

    public Point getCursorPos(List<String> lines, int x, int y) {
        if (lines.isEmpty()) {
            return new Point();
        }
        List<Line> measuredLines = measureLines(lines);
        y -= getStartY(measuredLines.size()) + this.y;
        int index = (int) (y / (getFontHeight()));
        if (index < 0) return new Point();
        if (index >= measuredLines.size())
            return new Point(measuredLines.get(measuredLines.size() - 1).getText().length(), measuredLines.size() - 1);
        Line line = measuredLines.get(index);
        x -= getStartX(line.getWidth()) + this.x;
        if (line.getWidth() <= 0) return new Point(0, index);
        if (line.getWidth() < x) return new Point(line.getText().length(), index);
        float currentX = 0;
        for (int i = 0; i < line.getText().length(); i++) {
            char c = line.getText().charAt(i);
            currentX += getFontRenderer().getCharWidth(c) * this.scale;
            if (currentX >= x) {
                return new Point(i, index);
            }
        }
        return new Point();
    }

    public Point2D.Float getPosOf(List<Line> measuredLines, Point cursorPos) {
        if (measuredLines.isEmpty()) {
            return new Point2D.Float(getStartX(0), getStartY(1));
        }
        Line line = measuredLines.get(cursorPos.y);
        String sub = line.getText().substring(0, Math.min(line.getText().length(), cursorPos.x));
        return new Point2D.Float(getStartX(line.getWidth()) + getFontRenderer().getStringWidth(sub) * this.scale, getStartY(measuredLines.size()) + cursorPos.y * getFontHeight());
    }

    @SideOnly(Side.CLIENT)
    public void drawMarked(float y0, float x0, float x1) {
        y0 -= 1;
        float y1 = y0 + getFontHeight();
        float red = Color.getRedF(this.markedColor);
        float green = Color.getGreenF(this.markedColor);
        float blue = Color.getBlueF(this.markedColor);
        float alpha = Color.getAlphaF(this.markedColor);
        if (alpha == 0)
            alpha = 1f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.disableTexture2D();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x0, y1, 0.0D).endVertex();
        bufferbuilder.pos(x1, y1, 0.0D).endVertex();
        bufferbuilder.pos(x1, y0, 0.0D).endVertex();
        bufferbuilder.pos(x0, y0, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    @SideOnly(Side.CLIENT)
    private void drawCursor(float x0, float y0) {
        x0 = (x0 - 0.8f) / this.scale;
        y0 = (y0 - 1) / this.scale;
        float x1 = x0 + 0.6f;
        float y1 = y0 + 9;
        float red = Color.getRedF(this.color);
        float green = Color.getGreenF(this.color);
        float blue = Color.getBlueF(this.color);
        float alpha = Color.getAlphaF(this.color);
        if (alpha == 0)
            alpha = 1f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.scale, this.scale, 0);
        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.disableTexture2D();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x0, y1, 0.0D).endVertex();
        bufferbuilder.pos(x1, y1, 0.0D).endVertex();
        bufferbuilder.pos(x1, y0, 0.0D).endVertex();
        bufferbuilder.pos(x0, y0, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
    }
}
