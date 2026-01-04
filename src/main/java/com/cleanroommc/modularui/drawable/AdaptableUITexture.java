package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import com.google.gson.JsonObject;

/**
 * This class is a <a href="https://en.wikipedia.org/wiki/9-slice_scaling">9-slice texture</a>. It can be created using
 * {@link UITexture.Builder#adaptable(int, int, int, int)}.
 */
public class AdaptableUITexture extends UITexture {

    private final int imageWidth, imageHeight, bl, bt, br, bb;
    private final boolean tiled;

    /**
     * Use {@link UITexture#builder()} with {@link Builder#adaptable(int, int)}
     */
    AdaptableUITexture(ResourceLocation location, float u0, float v0, float u1, float v1, ColorType colorType, boolean nonOpaque,
                       int imageWidth, int imageHeight, int bl, int bt, int br, int bb, boolean tiled) {
        super(location, u0, v0, u1, v1, colorType, nonOpaque);
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
        return new AdaptableUITexture(this.location, lerpU(uStart), lerpV(vStart), lerpU(uEnd), lerpV(vEnd), this.colorType, this.nonOpaque,
                this.imageWidth, this.imageHeight, this.bl, this.bt, this.br, this.bb, this.tiled);
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
        Platform.setupDrawTex(this.nonOpaque);
        Minecraft.getMinecraft().renderEngine.bindTexture(this.location);

        float uBl = this.bl * 1f / this.imageWidth, uBr = this.br * 1f / this.imageWidth;
        float vBt = this.bt * 1f / this.imageHeight, vBb = this.bb * 1f / this.imageHeight;
        float x1 = x + width, y1 = y + height;
        float uInnerStart = this.u0 + uBl, vInnerStart = this.v0 + vBt, uInnerEnd = this.u1 - uBr, vInnerEnd = this.v1 - vBb;

        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_TEX, buffer -> {
            if ((this.bl > 0 || this.br > 0) && this.bt <= 0 && this.bb <= 0) {
                // left border
                GuiDraw.drawTexture(buffer, x, y, x + this.bl, y1, this.u0, this.v0, uInnerStart, this.v1, 0);
                // right border
                GuiDraw.drawTexture(buffer, x1 - this.br, y, x1, y1, uInnerEnd, this.v0, this.u1, this.v1, 0);
                // center
                GuiDraw.drawTexture(buffer, x + this.bl, y, x1 - this.br, y1, uInnerStart, this.v0, uInnerEnd, this.v1, 0);
            } else if (this.bl <= 0 && this.br <= 0) {
                // top border
                GuiDraw.drawTexture(buffer, x, y, x1, y + this.bt, this.u0, this.v0, this.u1, vInnerStart, 0);
                // bottom border
                GuiDraw.drawTexture(buffer, x, y1 - this.bb, x1, y1, this.u0, vInnerEnd, this.u1, this.v1, 0);
                // center
                GuiDraw.drawTexture(buffer, x, y + this.bt, x1, y1 - this.bb, this.u0, vInnerStart, this.u1, vInnerEnd, 0);
            } else {
                // top left corner
                GuiDraw.drawTexture(buffer, x, y, x + this.bl, y + this.bt, this.u0, this.v0, uInnerStart, vInnerStart, 0);
                // top right corner
                GuiDraw.drawTexture(buffer, x1 - this.br, y, x1, y + this.bt, uInnerEnd, this.v0, this.u1, vInnerStart, 0);
                // bottom left corner
                GuiDraw.drawTexture(buffer, x, y1 - this.bb, x + this.bl, y1, this.u0, vInnerEnd, uInnerStart, this.v1, 0);
                // bottom right corner
                GuiDraw.drawTexture(buffer, x1 - this.br, y1 - this.bb, x1, y1, uInnerEnd, vInnerEnd, this.u1, this.v1, 0);

                // left border
                GuiDraw.drawTexture(buffer, x, y + this.bt, x + this.bl, y1 - this.bb, this.u0, vInnerStart, uInnerStart, vInnerEnd, 0);
                // top border
                GuiDraw.drawTexture(buffer, x + this.bl, y, x1 - this.br, y + this.bt, uInnerStart, this.v0, uInnerEnd, vInnerStart, 0);
                // right border
                GuiDraw.drawTexture(buffer, x1 - this.br, y + this.bt, x1, y1 - this.bb, uInnerEnd, vInnerStart, this.u1, vInnerEnd, 0);
                // bottom border
                GuiDraw.drawTexture(buffer, x + this.bl, y1 - this.bb, x1 - this.br, y1, uInnerStart, vInnerEnd, uInnerEnd, this.v1, 0);

                // center
                GuiDraw.drawTexture(buffer, x + this.bl, y + this.bt, x1 - this.br, y1 - this.bb, uInnerStart, vInnerStart, uInnerEnd, vInnerEnd, 0);
            }
        });
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public void drawTiled(float x, float y, float width, float height) {
        if (this.bl <= 0 && this.bt <= 0 && this.br <= 0 && this.bb <= 0) {
            GuiDraw.drawTiledTexture(this.location, x, y, width, height, this.u0, this.v0, this.u1, this.v1, this.imageWidth, this.imageHeight, 0);
            return;
        }
        Platform.setupDrawTex(this.nonOpaque);
        Minecraft.getMinecraft().renderEngine.bindTexture(this.location);

        float uBl = this.bl * 1f / this.imageWidth, uBr = this.br * 1f / this.imageWidth;
        float vBt = this.bt * 1f / this.imageHeight, vBb = this.bb * 1f / this.imageHeight;
        float x1 = x + width, y1 = y + height;
        float uInnerStart = this.u0 + uBl, vInnerStart = this.v0 + vBt, uInnerEnd = this.u1 - uBr, vInnerEnd = this.v1 - vBb;

        int tw = (int) (this.imageWidth * (this.u1 - this.u0));
        int th = (int) (this.imageHeight * (this.v1 - this.v0));

        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_TEX, buffer -> {
            if ((this.bl > 0 || this.br > 0) && this.bt <= 0 && this.bb <= 0) {
                // left border
                GuiDraw.drawTiledTexture(buffer, x, y, this.bl, height, this.u0, this.v0, uInnerStart, this.v1, this.bl, th, 0);
                // right border
                GuiDraw.drawTiledTexture(buffer, x1 - this.br, y, this.br, height, uInnerEnd, this.v0, this.u1, this.v1, this.br, th, 0);
                // center
                GuiDraw.drawTiledTexture(buffer, x + this.bl, y, width - this.bl - this.br, height, uInnerStart, this.v0, uInnerEnd, this.v1, tw - this.bl - this.br, th, 0);
            } else if (this.bl <= 0 && this.br <= 0) {
                // top border
                GuiDraw.drawTiledTexture(buffer, x, y, width, this.bt, this.u0, this.v0, this.u1, vInnerStart, tw, this.bt, 0);
                // bottom border
                GuiDraw.drawTiledTexture(buffer, x, y1 - this.bb, width, this.bb, this.u0, vInnerEnd, this.u1, this.v1, tw, this.bb, 0);
                // center
                GuiDraw.drawTiledTexture(buffer, x, y + this.bt, width, height - this.bt - this.bb, this.u0, vInnerStart, this.u1, vInnerEnd, tw, th - this.bt - this.bb, 0);
            } else {
                // corners don't need to be tiled
                // they are drawn at their size
                // top left corner
                GuiDraw.drawTexture(buffer, x, y, x + this.bl, y + this.bt, this.u0, this.v0, uInnerStart, vInnerStart, 0);
                // top right corner
                GuiDraw.drawTexture(buffer, x1 - this.br, y, x1, y + this.bt, uInnerEnd, this.v0, this.u1, vInnerStart, 0);
                // bottom left corner
                GuiDraw.drawTexture(buffer, x, y1 - this.bb, x + this.bl, y1, this.u0, vInnerEnd, uInnerStart, this.v1, 0);
                // bottom right corner
                GuiDraw.drawTexture(buffer, x1 - this.br, y1 - this.bb, x1, y1, uInnerEnd, vInnerEnd, this.u1, this.v1, 0);

                // left border
                GuiDraw.drawTiledTexture(buffer, x, y + this.bt, this.bl, height - this.bt - this.bb, this.u0, vInnerStart, uInnerStart, vInnerEnd, this.bl, th - this.bt - this.bb, 0);
                // top border
                GuiDraw.drawTiledTexture(buffer, x + this.bl, y, width - this.bl - this.br, this.bt, uInnerStart, this.v0, uInnerEnd, vInnerStart, tw - this.bl - this.bb, this.bt, 0);
                // right border
                GuiDraw.drawTiledTexture(buffer, x1 - this.br, y + this.bt, this.br, height - this.bt - this.bb, uInnerEnd, vInnerStart, this.u1, vInnerEnd, this.br, th - this.bt - this.bb, 0);
                // bottom border
                GuiDraw.drawTiledTexture(buffer, x + this.bl, y1 - this.bb, width - this.bl - this.br, this.bb, uInnerStart, vInnerEnd, uInnerEnd, this.v1, tw - this.bl - this.br, this.bb, 0);

                // center
                GuiDraw.drawTiledTexture(buffer, x + this.bl, y + this.bt, width - this.bl - this.br, height - this.bt - this.bb, uInnerStart, vInnerStart, uInnerEnd, vInnerEnd, tw - this.bl - this.br, th - this.bt - this.bb, 0);
            }
        });
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        super.saveToJson(json);
        if (json.entrySet().size() == 1) return true;
        json.addProperty("imageWidth", this.imageWidth);
        json.addProperty("imageHeight", this.imageHeight);
        json.addProperty("bl", this.bl);
        json.addProperty("br", this.br);
        json.addProperty("bt", this.bt);
        json.addProperty("bb", this.bb);
        json.addProperty("tiled", this.tiled);
        return true;
    }

    @Override
    protected AdaptableUITexture copy() {
        return new AdaptableUITexture(location, u0, v0, u1, v1, colorType, nonOpaque, imageWidth, imageHeight, bl, bt, br, bb, tiled);
    }

    @Override
    public AdaptableUITexture withColorOverride(int color) {
        return (AdaptableUITexture) super.withColorOverride(color);
    }
}
