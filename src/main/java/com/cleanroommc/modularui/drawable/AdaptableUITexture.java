package com.cleanroommc.modularui.drawable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

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
        return new AdaptableUITexture(location, calcU(uStart), calcV(vStart), calcU(uEnd), calcV(vEnd), canApplyTheme, imageWidth, imageHeight, borderX, borderY, tiled);
    }

    @Override
    public void draw(float x, float y, float width, float height) {
        if (width == imageWidth && height == imageHeight) {
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
        if (borderX <= 0 && borderY <= 0) {
            super.draw(x, y, width, height);
            return;
        }
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(location);

        float uB = borderX * 1f / imageWidth;
        float vB = borderY * 1f / imageHeight;
        float x1 = x + width, y1 = y + height;

        if (borderX > 0 && borderY <= 0) {
            // left border
            GuiDraw.drawTexture(x, y, x + borderX, y1, u0, v0, u0 + uB, v1);
            // right border
            GuiDraw.drawTexture(x1 - borderX, y, x1, y1, u1 - uB, v0, u1, v1);
            // center
            GuiDraw.drawTexture(x + borderX, y, x1 - borderX, y1, uB, v0, u1 - uB, v1);
        } else if (borderX <= 0) {
            // top border
            GuiDraw.drawTexture(x, y, x1, y + borderY, u0, v0, u1, v0 + uB);
            // bottom border
            GuiDraw.drawTexture(x, y1 - borderY, x1, y1, u0, v1 - uB, u1, v1);
            // center
            GuiDraw.drawTexture(x, y + borderY, x1, y1 - borderY, u0, vB, u1, v1 - vB);
        } else {
            // top left corner
            GuiDraw.drawTexture(x, y, x + borderX, y + borderY, u0, v0, u0 + uB, v0 + vB);
            // top right corner
            GuiDraw.drawTexture(x1 - borderX, y, x1, y + borderY, u1 - uB, v0, u1, vB);
            // bottom left corner
            GuiDraw.drawTexture(x, y1 - borderY, x + borderX, y1, u0, v1 - vB, uB, v1);
            // bottom right corner
            GuiDraw.drawTexture(x1 - borderX, y1 - borderY, x1, y1, u1 - uB, v1 - vB, u1, v1);

            // left border
            GuiDraw.drawTexture(x, y + borderY, x + borderX, y1 - borderY, u0, vB, uB, v1 - vB);
            // top border
            GuiDraw.drawTexture(x + borderX, y, x1 - borderX, y + borderY, uB, v0, u1 - uB, vB);
            // right border
            GuiDraw.drawTexture(x1 - borderX, y + borderY, x1, y1 - borderY, u1 - uB, vB, u1, v1 - vB);
            // bottom border
            GuiDraw.drawTexture(x + borderX, y1 - borderY, x1 - borderX, y1, uB, v1 - vB, u1 - uB, v1);

            // center
            GuiDraw.drawTexture(x + borderX, y + borderY, x1 - borderX, y1 - borderY, uB, vB, u1 - uB, v1 - vB);
        }
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public void drawTiled(float x, float y, float width, float height) {
        if (borderX <= 0 && borderY <= 0) {
            GuiDraw.drawTiledTexture(this.location, x, y, width, height, u0, v0, u1, v1, imageWidth, imageHeight, 0);
            return;
        }
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(location);

        float uB = borderX * 1f / imageWidth;
        float vB = borderY * 1f / imageHeight;
        float x1 = x + width, y1 = y + height;

        if (borderX > 0 && borderY <= 0) {
            // left border
            GuiDraw.drawTiledTexture(x, y, borderX, height, u0, v0, u0 + uB, v1, borderX, imageHeight, 0);
            // right border
            GuiDraw.drawTiledTexture(x1 - borderX, y, borderX, height, u1 - uB, v0, u1, v1, borderX, imageHeight, 0);
            // center
            GuiDraw.drawTiledTexture(x + borderX, y, width - 2 * borderX, height, uB, v0, u1 - uB, v1, imageHeight - 2 * borderX, imageHeight, 0);
        } else if (borderX <= 0) {
            // top border
            GuiDraw.drawTiledTexture(x, y, width, borderY, u0, v0, u1, v0 + uB, imageWidth, borderY, 0);
            // bottom border
            GuiDraw.drawTiledTexture(x, y1 - borderY, width, borderY, u0, v1 - uB, u1, v1, imageWidth, borderY, 0);
            // center
            GuiDraw.drawTiledTexture(x, y + borderY, width, height - 2 * borderY, u0, vB, u1, v1 - vB, imageWidth, imageHeight - 2 * borderY, 0);
        } else {
            // top left corner
            GuiDraw.drawTiledTexture(x, y, borderX, borderY, u0, v0, u0 + uB, v0 + vB, borderX, borderY, 0);
            // top right corner
            GuiDraw.drawTiledTexture(x1 - borderX, y, borderX, borderY, u1 - uB, v0, u1, vB, borderX, borderY, 0);
            // bottom left corner
            GuiDraw.drawTiledTexture(x, y1 - borderY, borderX, borderY, u0, v1 - vB, uB, v1, borderX, borderY, 0);
            // bottom right corner
            GuiDraw.drawTiledTexture(x1 - borderX, y1 - borderY, borderX, borderY, u1 - uB, v1 - vB, u1, v1, borderX, borderY, 0);

            // left border
            GuiDraw.drawTiledTexture(x, y + borderY, borderX, height - 2 * borderY, u0, vB, uB, v1 - vB, borderX, imageHeight - 2 * borderY, 0);
            // top border
            GuiDraw.drawTiledTexture(x + borderX, y, width - 2 * borderX, borderY, uB, v0, u1 - uB, vB, imageWidth - 2 * borderX, borderY, 0);
            // right border
            GuiDraw.drawTiledTexture(x1 - borderX, y + borderY, borderX, height - 2 * borderY, u1 - uB, vB, u1, v1 - vB, borderX, imageHeight - 2 * borderY, 0);
            // bottom border
            GuiDraw.drawTiledTexture(x + borderX, y1 - borderY, width - 2 * borderX, borderY, uB, v1 - vB, u1 - uB, v1, imageWidth - 2 * borderX, borderY, 0);

            // center
            GuiDraw.drawTiledTexture(x + borderX, y + borderY, width - 2 * borderX, height - 2 * borderY, uB, vB, u1 - uB, v1 - vB, imageWidth - 2 * borderX, imageHeight - 2 * borderY, 0);
        }
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }
}
