package com.cleanroommc.modularui.drawable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class AdaptableUITexture extends UITexture {

    private final int imageWidth, imageHeight, bl, bt, br, bb;
    private final boolean tiled;

    /**
     * Use {@link UITexture#builder()} with {@link Builder#adaptable(int, int)}
     */
    AdaptableUITexture(ResourceLocation location, float u0, float v0, float u1, float v1, boolean background, int imageWidth, int imageHeight, int bl, int bt, int br, int bb, boolean tiled) {
        super(location, u0, v0, u1, v1, background);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.bl = bl;
        this.bt = bt;
        this.br = br;
        this.bb = bb;
        this.tiled = tiled;
    }

    @Override
    public AdaptableUITexture getSubArea(float uStart, float vStart, float uEnd, float vEnd) {
        return new AdaptableUITexture(this.location, lerpU(uStart), lerpV(vStart), lerpU(uEnd), lerpV(vEnd), this.canApplyTheme, this.imageWidth, this.imageHeight, this.bl, this.bt, this.br, this.bb, this.tiled);
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
        if (this.bl <= 0 && this.bt <= 0 && this.br <= 0 && this.bb <= 0) {
            super.draw(x, y, width, height);
            return;
        }
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(this.location);

        float uBl = this.bl * 1f / this.imageWidth, uBr = this.br * 1f / this.imageWidth;
        float vBt = this.bt * 1f / this.imageHeight, vBb = this.bb * 1f / this.imageHeight;
        float x1 = x + width, y1 = y + height;

        if ((this.bl > 0 || this.br > 0) && this.bt <= 0 && this.bb <= 0) {
            // left border
            GuiDraw.drawTexture(x, y, x + this.bl, y1, this.u0, this.v0, this.u0 + uBl, this.v1);
            // right border
            GuiDraw.drawTexture(x1 - this.br, y, x1, y1, this.u1 - uBr, this.v0, this.u1, this.v1);
            // center
            GuiDraw.drawTexture(x + this.bl, y, x1 - this.br, y1, uBl, this.v0, this.u1 - uBr, this.v1);
        } else if (this.bl <= 0 && this.br <= 0) {
            // top border
            GuiDraw.drawTexture(x, y, x1, y + this.bt, this.u0, this.v0, this.u1, this.v0 + vBt);
            // bottom border
            GuiDraw.drawTexture(x, y1 - this.bb, x1, y1, this.u0, this.v1 - vBb, this.u1, this.v1);
            // center
            GuiDraw.drawTexture(x, y + this.bt, x1, y1 - this.bb, this.u0, vBt, this.u1, this.v1 - vBb);
        } else {
            // top left corner
            GuiDraw.drawTexture(x, y, x + this.bl, y + this.bt, this.u0, this.v0, this.u0 + uBl, this.v0 + vBt);
            // top right corner
            GuiDraw.drawTexture(x1 - this.br, y, x1, y + this.bt, this.u1 - uBr, this.v0, this.u1, vBt);
            // bottom left corner
            GuiDraw.drawTexture(x, y1 - this.bb, x + this.bl, y1, this.u0, this.v1 - vBb, uBl, this.v1);
            // bottom right corner
            GuiDraw.drawTexture(x1 - this.br, y1 - this.bb, x1, y1, this.u1 - uBr, this.v1 - vBb, this.u1, this.v1);

            // left border
            GuiDraw.drawTexture(x, y + this.bt, x + this.bl, y1 - this.bb, this.u0, vBt, uBl, this.v1 - vBb);
            // top border
            GuiDraw.drawTexture(x + this.bl, y, x1 - this.br, y + this.bt, uBl, this.v0, this.u1 - uBl, vBt);
            // right border
            GuiDraw.drawTexture(x1 - this.br, y + this.bt, x1, y1 - this.bb, this.u1 - uBr, vBt, this.u1, this.v1 - vBb);
            // bottom border
            GuiDraw.drawTexture(x + this.bl, y1 - this.bb, x1 - this.br, y1, uBl, this.v1 - vBb, this.u1 - uBl, this.v1);

            // center
            GuiDraw.drawTexture(x + this.bl, y + this.bt, x1 - this.br, y1 - this.bb, uBl, vBt, this.u1 - uBr, this.v1 - vBb);
        }
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public void drawTiled(float x, float y, float width, float height) {
        if (this.bl <= 0 && this.bt <= 0 && this.br <= 0 && this.bb <= 0) {
            GuiDraw.drawTiledTexture(this.location, x, y, width, height, this.u0, this.v0, this.u1, this.v1, this.imageWidth, this.imageHeight, 0);
            return;
        }
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(this.location);

        float uBl = this.bl * 1f / this.imageWidth, uBr = this.br * 1f / this.imageWidth;
        float vBt = this.bt * 1f / this.imageHeight, vBb = this.bb * 1f / this.imageHeight;
        float x1 = x + width, y1 = y + height;

        int tw = (int) (this.imageWidth * (this.u1 - this.u0));
        int th = (int) (this.imageHeight * (this.v1 - this.v0));

        if ((this.bl > 0 || this.br > 0) && this.bt <= 0 && this.bb <= 0) {
            // left border
            GuiDraw.drawTiledTexture(x, y, this.bl, height, this.u0, this.v0, this.u0 + uBl, this.v1, this.bl, th, 0);
            // right border
            GuiDraw.drawTiledTexture(x1 - this.br, y, this.br, height, this.u1 - uBr, this.v0, this.u1, this.v1, this.br, th, 0);
            // center
            GuiDraw.drawTiledTexture(x + this.bl, y, width - this.bl - this.br, height, uBl, this.v0, this.u1 - uBr, this.v1, tw - this.bl - this.br, th, 0);
        } else if (this.bl <= 0 && this.br <= 0) {
            // top border
            GuiDraw.drawTiledTexture(x, y, width, this.bt, this.u0, this.v0, this.u1, this.v0 + vBt, tw, this.bt, 0);
            // bottom border
            GuiDraw.drawTiledTexture(x, y1 - this.bb, width, this.bb, this.u0, this.v1 - vBb, this.u1, this.v1, tw, this.bb, 0);
            // center
            GuiDraw.drawTiledTexture(x, y + this.bt, width, height - this.bt - this.bb, this.u0, vBt, this.u1, this.v1 - vBb, tw, th - this.bt - this.bb, 0);
        } else {
            // top left corner
            GuiDraw.drawTiledTexture(x, y, this.bl, this.bt, this.u0, this.v0, this.u0 + uBl, this.v0 + vBt, this.bl, this.bt, 0);
            // top right corner
            GuiDraw.drawTiledTexture(x1 - this.br, y, this.br, this.bt, this.u1 - uBr, this.v0, this.u1, vBt, this.br, this.bt, 0);
            // bottom left corner
            GuiDraw.drawTiledTexture(x, y1 - this.bb, this.bl, this.bb, this.u0, this.v1 - vBb, uBl, this.v1, this.bl, this.bb, 0);
            // bottom right corner
            GuiDraw.drawTiledTexture(x1 - this.br, y1 - this.bb, this.br, this.bb, this.u1 - uBr, this.v1 - vBb, this.u1, this.v1, this.br, this.bb, 0);

            // left border
            GuiDraw.drawTiledTexture(x, y + this.bt, this.bl, height - this.bt - this.bb, this.u0, vBt, uBl, this.v1 - vBb, this.bl, th - this.bt - this.bb, 0);
            // top border
            GuiDraw.drawTiledTexture(x + this.bl, y, width - this.bl - this.br, this.bt, uBl, this.v0, this.u1 - uBr, vBt, tw - this.bl - this.bb, this.bt, 0);
            // right border
            GuiDraw.drawTiledTexture(x1 - this.br, y + this.bt, this.br, height - this.bt - this.bb, this.u1 - uBr, vBt, this.u1, this.v1 - vBb, this.br, th - this.bt - this.bb, 0);
            // bottom border
            GuiDraw.drawTiledTexture(x + this.bl, y1 - this.bb, width - this.bl - this.br, this.bb, uBl, this.v1 - vBb, this.u1 - uBr, this.v1, tw - this.bl - this.br, this.bb, 0);

            // center
            GuiDraw.drawTiledTexture(x + this.bl, y + this.bt, width - this.bl - this.br, height - this.bt - this.bb, uBl, vBt, this.u1 - uBr, this.v1 - vBb, tw - this.bl - this.br, th - this.bt - this.bb, 0);
        }
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }
}
