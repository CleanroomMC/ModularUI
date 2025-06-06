package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

public class TextFieldRenderer extends TextRenderer {

    protected final TextFieldHandler handler;
    protected int markedColor = 0x2F72A8;
    protected int cursorColor = 0xFFFFFFFF;
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

    public void setCursorColor(int cursorColor) {
        this.cursorColor = cursorColor;
    }

    @Override
    protected void drawMeasuredLines(List<Line> measuredLines) {
        drawMarked(measuredLines);
        super.drawMeasuredLines(measuredLines);
        // draw cursor
        if (this.renderCursor) {
            Point main = this.handler.getMainCursor();
            Point2D.Float start = getPosOf(measuredLines, main);
            if (this.handler.getText().get(main.y).isEmpty()) {
                start.x += 0.7f;
            }
            drawCursor(start.x, start.y);
        }
    }

    @Override
    public List<String> wrapLine(String line) {
        return Collections.singletonList(line);
    }

    protected void drawMarked(List<Line> measuredLines) {
        if (!this.simulate && this.handler.hasTextMarked()) {
            Point2D.Float start = getPosOf(measuredLines, this.handler.getStartCursor());
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
    }

    public Point getCursorPos(List<String> lines, int x, int y) {
        if (lines.isEmpty()) {
            return new Point();
        }
        List<Line> measuredLines = measureLines(lines);
        y -= getStartY(measuredLines.size());
        int index = (int) (y / (getFontHeight()));
        if (index < 0) return new Point();
        if (index >= measuredLines.size())
            return new Point(measuredLines.get(measuredLines.size() - 1).getText().length(), measuredLines.size() - 1);
        Line line = measuredLines.get(index);
        x -= getStartX(line.getWidth());
        if (line.getWidth() <= 0) return new Point(0, index);
        if (line.getWidth() < x) return new Point(line.getText().length(), index);
        float currentX = 0;
        for (int i = 0; i < line.getText().length(); i++) {
            char c = line.getText().charAt(i);
            float charWidth = getFontRenderer().getCharWidth(c) * this.scale;
            currentX += charWidth;
            if (currentX >= x) {
                // dist with current letter < dist without current letter -> next letter pos
                if (Math.abs(currentX - x) < Math.abs(currentX - charWidth - x)) i++;
                return new Point(i, index);
            }
        }
        return new Point();
    }

    public Point2D.Float getPosOf(List<Line> measuredLines, Point cursorPos) {
        if (measuredLines.isEmpty()) {
            return new Point2D.Float(getStartX(0), getStartYOfLines(1));
        }
        Line line = measuredLines.get(cursorPos.y);
        String sub = line.getText().substring(0, Math.min(line.getText().length(), cursorPos.x));
        return new Point2D.Float(getStartX(line.getWidth()) + getFontRenderer().getStringWidth(sub) * this.scale, getStartYOfLines(measuredLines.size()) + cursorPos.y * getFontHeight());
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
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.scale, this.scale, 0);
        drawRect(x0, y0, x1, y1, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    private void drawCursor(float x0, float y0) {
        x0 = (x0 - 0.8f) / this.scale;
        y0 = (y0 - 1) / this.scale;
        float x1 = x0 + 0.6f;
        float y1 = y0 + 9;
        float red = Color.getRedF(this.cursorColor);
        float green = Color.getGreenF(this.cursorColor);
        float blue = Color.getBlueF(this.cursorColor);
        float alpha = Color.getAlphaF(this.cursorColor);
        if (alpha == 0)
            alpha = 1f;
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.scale, this.scale, 0);
        drawRect(x0, y0, x1, y1, red, green, blue, alpha);
        GlStateManager.popMatrix();
    }

    private static void drawRect(float x0, float y0, float x1, float y1, float red, float green, float blue, float alpha) {
        Platform.setupDrawColor();
        GlStateManager.color(red, green, blue, alpha);
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS, bufferBuilder -> {
            bufferBuilder.pos(x0, y1, 0.0D).endVertex();
            bufferBuilder.pos(x1, y1, 0.0D).endVertex();
            bufferBuilder.pos(x1, y0, 0.0D).endVertex();
            bufferBuilder.pos(x0, y0, 0.0D).endVertex();
        });
        GlStateManager.color(1, 1, 1, 1);
    }
}
