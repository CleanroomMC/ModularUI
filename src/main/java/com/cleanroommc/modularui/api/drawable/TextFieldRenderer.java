package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TextFieldRenderer extends TextRenderer {

    private int cursor, cursorEnd;
    private int markedColor = 0x2F72A8;
    private boolean drawingMarked = false;
    private boolean renderCursor = false;

    public TextFieldRenderer(Pos2d pos, int color, int maxWidth) {
        super(pos, color, maxWidth);
    }

    public void setMarkedColor(int markedColor) {
        this.markedColor = markedColor;
    }

    public void setCursor(int cursor, int cursorEnd) {
        this.cursor = cursor;
        this.cursorEnd = cursorEnd;
    }

    public void toggleRenderCursor() {
        this.renderCursor = !renderCursor;
    }

    public void setRenderCursor(boolean renderCursor) {
        this.renderCursor = renderCursor;
    }

    @Override
    public void draw(String text) {
        drawingMarked = false;
        if (text.isEmpty() || cursor == 0) {
            if (renderCursor) {
                drawCursor(currentX, getCurrentY());
            }
            if (text.isEmpty()) {
                return;
            }
        }
        super.draw(text);
    }

    @Override
    protected void addChar(char c, boolean addToWidth) {
        super.addChar(c, addToWidth);
        if (currentIndex == cursor) {
            if (renderCursor) {
                newWord(false);
                drawCursor(currentX, getCurrentY());
            }
            if (drawingMarked) {
                if (wordWith > 0) {
                    newWord(false);
                }
                drawingMarked = false;
            } else if (cursor != cursorEnd) {
                if (wordWith > 0) {
                    newWord(false);
                }
                drawingMarked = true;
            }
        } else if (currentIndex == cursorEnd) {
            newWord(false);
            drawingMarked = !drawingMarked;
        }
    }

    @Override
    protected void renderOther(float scaleFactor) {
        super.renderOther(scaleFactor);
        if (drawingMarked) {
            drawSelectionBox(currentX * scaleFactor, getCurrentY() * scaleFactor, wordWith - 1);
        }
    }

    @SideOnly(Side.CLIENT)
    private void drawCursor(float x, float y) {
        x -= 0.8f;
        y -= 1;
        x *= (1 / getScale());
        y *= (1 / getScale());
        float endX = x + 0.6f /* * (1 / getScale())*/;
        float endY = y + 9;
        float red = Color.getRedF(getDefaultColor());
        float green = Color.getGreenF(getDefaultColor());
        float blue = Color.getBlueF(getDefaultColor());
        float alpha = Color.getAlphaF(getDefaultColor());
        if (alpha == 0)
            alpha = 1f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(getScale(), getScale(), 0);
        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.disableTexture2D();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, y, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
    }

    @SideOnly(Side.CLIENT)
    private void drawSelectionBox(float x, float y, float width) {
        float endX = x + width;
        y -= 1;
        float endY = y + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        float red = (float) (markedColor >> 16 & 255) / 255.0F;
        float green = (float) (markedColor >> 8 & 255) / 255.0F;
        float blue = (float) (markedColor & 255) / 255.0F;
        float alpha = (float) (markedColor >> 24 & 255) / 255.0F;
        if (alpha == 0)
            alpha = 1f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.disableTexture2D();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, y, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }
}
