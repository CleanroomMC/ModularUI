package com.cleanroommc.modularui.drawable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class AdaptableUITexture extends UITexture {

    private final int imageWidth, imageHeight, borderX, borderY;
    private final boolean tiled;

    /**
     * Use {@link UITexture#builder()} with {@link Builder#adaptable(int, int)}
     */
    AdaptableUITexture(ResourceLocation location, float u0, float v0, float u1, float v1, boolean background, int imageWidth, int imageHeight, int borderX, int borderY, boolean tiled) {
        super(location, u0, v0, u1, v1, background);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.borderX = borderX;
        this.borderY = borderY;
        this.tiled = tiled;
    }

    @Override
    public AdaptableUITexture getSubArea(float uStart, float vStart, float uEnd, float vEnd) {
        return new AdaptableUITexture(this.location, calcU(uStart), calcV(vStart), calcU(uEnd), calcV(vEnd), this.canApplyTheme, this.imageWidth, this.imageHeight, this.borderX, this.borderY, this.tiled);
    }

    @Override
    public void draw(float x, float y, float width, float height) {
        if (width == this.imageWidth && height == this.imageHeight) {
            super.draw(x, y, width, height);
            return;
        }
        if (this.tiled) {
            drawTiled(x, y, width, height);
        } else {
            drawStretched(x, y, width, height);
        }
    }

    public void drawStretched(float x, float y, float width, float height) {
        if (this.borderX <= 0 && this.borderY <= 0) {
            super.draw(x, y, width, height);
            return;
        }
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(this.location);

        float uB = this.borderX * 1f / this.imageWidth;
        float vB = this.borderY * 1f / this.imageHeight;
        float x1 = x + width, y1 = y + height;

        if (this.borderX > 0 && this.borderY <= 0) {
            // left border
            GuiDraw.drawTexture(x, y, x + this.borderX, y1, this.u0, this.v0, this.u0 + uB, this.v1);
            // right border
            GuiDraw.drawTexture(x1 - this.borderX, y, x1, y1, this.u1 - uB, this.v0, this.u1, this.v1);
            // center
            GuiDraw.drawTexture(x + this.borderX, y, x1 - this.borderX, y1, uB, this.v0, this.u1 - uB, this.v1);
        } else if (this.borderX <= 0) {
            // top border
            GuiDraw.drawTexture(x, y, x1, y + this.borderY, this.u0, this.v0, this.u1, this.v0 + uB);
            // bottom border
            GuiDraw.drawTexture(x, y1 - this.borderY, x1, y1, this.u0, this.v1 - uB, this.u1, this.v1);
            // center
            GuiDraw.drawTexture(x, y + this.borderY, x1, y1 - this.borderY, this.u0, vB, this.u1, this.v1 - vB);
        } else {
            // top left corner
            GuiDraw.drawTexture(x, y, x + this.borderX, y + this.borderY, this.u0, this.v0, this.u0 + uB, this.v0 + vB);
            // top right corner
            GuiDraw.drawTexture(x1 - this.borderX, y, x1, y + this.borderY, this.u1 - uB, this.v0, this.u1, vB);
            // bottom left corner
            GuiDraw.drawTexture(x, y1 - this.borderY, x + this.borderX, y1, this.u0, this.v1 - vB, uB, this.v1);
            // bottom right corner
            GuiDraw.drawTexture(x1 - this.borderX, y1 - this.borderY, x1, y1, this.u1 - uB, this.v1 - vB, this.u1, this.v1);

            // left border
            GuiDraw.drawTexture(x, y + this.borderY, x + this.borderX, y1 - this.borderY, this.u0, vB, uB, this.v1 - vB);
            // top border
            GuiDraw.drawTexture(x + this.borderX, y, x1 - this.borderX, y + this.borderY, uB, this.v0, this.u1 - uB, vB);
            // right border
            GuiDraw.drawTexture(x1 - this.borderX, y + this.borderY, x1, y1 - this.borderY, this.u1 - uB, vB, this.u1, this.v1 - vB);
            // bottom border
            GuiDraw.drawTexture(x + this.borderX, y1 - this.borderY, x1 - this.borderX, y1, uB, this.v1 - vB, this.u1 - uB, this.v1);

            // center
            GuiDraw.drawTexture(x + this.borderX, y + this.borderY, x1 - this.borderX, y1 - this.borderY, uB, vB, this.u1 - uB, this.v1 - vB);
        }
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public void drawTiled(float x, float y, float width, float height) {
        if (this.borderX <= 0 && this.borderY <= 0) {
            GuiDraw.drawTiledTexture(this.location, x, y, width, height, this.u0, this.v0, this.u1, this.v1, this.imageWidth, this.imageHeight, 0);
            return;
        }
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(this.location);

        float uB = this.borderX * 1f / this.imageWidth;
        float vB = this.borderY * 1f / this.imageHeight;
        float x1 = x + width, y1 = y + height;

        if (this.borderX > 0 && this.borderY <= 0) {
            // left border
            GuiDraw.drawTiledTexture(x, y, this.borderX, height, this.u0, this.v0, this.u0 + uB, this.v1, this.borderX, this.imageHeight, 0);
            // right border
            GuiDraw.drawTiledTexture(x1 - this.borderX, y, this.borderX, height, this.u1 - uB, this.v0, this.u1, this.v1, this.borderX, this.imageHeight, 0);
            // center
            GuiDraw.drawTiledTexture(x + this.borderX, y, width - 2 * this.borderX, height, uB, this.v0, this.u1 - uB, this.v1, this.imageHeight - 2 * this.borderX, this.imageHeight, 0);
        } else if (this.borderX <= 0) {
            // top border
            GuiDraw.drawTiledTexture(x, y, width, this.borderY, this.u0, this.v0, this.u1, this.v0 + uB, this.imageWidth, this.borderY, 0);
            // bottom border
            GuiDraw.drawTiledTexture(x, y1 - this.borderY, width, this.borderY, this.u0, this.v1 - uB, this.u1, this.v1, this.imageWidth, this.borderY, 0);
            // center
            GuiDraw.drawTiledTexture(x, y + this.borderY, width, height - 2 * this.borderY, this.u0, vB, this.u1, this.v1 - vB, this.imageWidth, this.imageHeight - 2 * this.borderY, 0);
        } else {
            // top left corner
            GuiDraw.drawTiledTexture(x, y, this.borderX, this.borderY, this.u0, this.v0, this.u0 + uB, this.v0 + vB, this.borderX, this.borderY, 0);
            // top right corner
            GuiDraw.drawTiledTexture(x1 - this.borderX, y, this.borderX, this.borderY, this.u1 - uB, this.v0, this.u1, vB, this.borderX, this.borderY, 0);
            // bottom left corner
            GuiDraw.drawTiledTexture(x, y1 - this.borderY, this.borderX, this.borderY, this.u0, this.v1 - vB, uB, this.v1, this.borderX, this.borderY, 0);
            // bottom right corner
            GuiDraw.drawTiledTexture(x1 - this.borderX, y1 - this.borderY, this.borderX, this.borderY, this.u1 - uB, this.v1 - vB, this.u1, this.v1, this.borderX, this.borderY, 0);

            // left border
            GuiDraw.drawTiledTexture(x, y + this.borderY, this.borderX, height - 2 * this.borderY, this.u0, vB, uB, this.v1 - vB, this.borderX, this.imageHeight - 2 * this.borderY, 0);
            // top border
            GuiDraw.drawTiledTexture(x + this.borderX, y, width - 2 * this.borderX, this.borderY, uB, this.v0, this.u1 - uB, vB, this.imageWidth - 2 * this.borderX, this.borderY, 0);
            // right border
            GuiDraw.drawTiledTexture(x1 - this.borderX, y + this.borderY, this.borderX, height - 2 * this.borderY, this.u1 - uB, vB, this.u1, this.v1 - vB, this.borderX, this.imageHeight - 2 * this.borderY, 0);
            // bottom border
            GuiDraw.drawTiledTexture(x + this.borderX, y1 - this.borderY, width - 2 * this.borderX, this.borderY, uB, this.v1 - vB, this.u1 - uB, this.v1, this.imageWidth - 2 * this.borderX, this.borderY, 0);

            // center
            GuiDraw.drawTiledTexture(x + this.borderX, y + this.borderY, width - 2 * this.borderX, height - 2 * this.borderY, uB, vB, this.u1 - uB, this.v1 - vB, this.imageWidth - 2 * this.borderX, this.imageHeight - 2 * this.borderY, 0);
        }
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }
}
