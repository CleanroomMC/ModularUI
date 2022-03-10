package com.cleanroommc.modularui.api.drawable;

import net.minecraft.util.ResourceLocation;

public class AdaptableUITexture extends UITexture {

    private final int imageWidth, imageHeight, borderWidthU, borderWidthV;

    public AdaptableUITexture(ResourceLocation location, float u0, float v0, float u1, float v1, int imageWidth, int imageHeight, int borderWidthU, int borderWidthV) {
        super(location, u0, v0, u1, v1);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.borderWidthU = borderWidthU;
        this.borderWidthV = borderWidthV;
    }

    public AdaptableUITexture(ResourceLocation location, int imageWidth, int imageHeight, int borderWidthU, int borderWidthV) {
        this(location, 0, 0, 1, 1, imageWidth, imageHeight, borderWidthU, borderWidthV);
    }

    public static AdaptableUITexture of(ResourceLocation location, int imageWidth, int imageHeight, int borderWidthU, int borderWidthV) {
        return new AdaptableUITexture(location, imageWidth, imageHeight, borderWidthU, borderWidthV);
    }

    public static AdaptableUITexture of(ResourceLocation location, int imageWidth, int imageHeight, int borderWidthPixel) {
        return new AdaptableUITexture(location, imageWidth, imageHeight, borderWidthPixel, borderWidthPixel);
    }

    public static AdaptableUITexture of(String location, int imageWidth, int imageHeight, int borderWidthPixel) {
        return new AdaptableUITexture(new ResourceLocation(location), imageWidth, imageHeight, borderWidthPixel, borderWidthPixel);
    }

    public static AdaptableUITexture of(String mod, String location, int imageWidth, int imageHeight, int borderWidthPixel) {
        return new AdaptableUITexture(new ResourceLocation(mod, location), imageWidth, imageHeight, borderWidthPixel, borderWidthPixel);
    }

    @Override
    public AdaptableUITexture getSubArea(float u0, float v0, float u1, float v1) {
        return new AdaptableUITexture(location, calcUV0(this.u0, u0), calcUV0(this.v0, v0), this.u1 * u1, this.v1 * v1, imageWidth, imageHeight, borderWidthU, borderWidthV);
    }

    @Override
    public AdaptableUITexture exposeToJson() {
        return (AdaptableUITexture) super.exposeToJson();
    }

    @Override
    public void draw(float x, float y, float width, float height) {
        float borderU =  borderWidthU * 1f / imageWidth;
        float borderV = borderWidthV * 1f / imageHeight;
        // draw corners
        draw(location, x, y, borderWidthU, borderWidthV, u0, v0, borderU, borderV); // x0 y0
        draw(location, x + width - borderWidthU, y, borderWidthU, borderWidthV, u1 - borderU, v0, u1, borderV); // x1 y0
        draw(location, x, y + height - borderWidthV, borderWidthU, borderWidthV, u0, v1 - borderV, borderU, v1); // x0 y1
        draw(location, x + width - borderWidthU, y + height - borderWidthV, borderWidthU, borderWidthV, u1 - borderU, v1 - borderV, u1, v1); // x1 y1
        // draw edges
        draw(location, x + borderWidthU, y, width - borderWidthU * 2, borderWidthV, borderU, v0, u1 - borderU, borderV); // top
        draw(location, x + borderWidthU, y + height - borderWidthV, width - borderWidthU * 2, borderWidthV, borderU, v1 - borderV, u1 - borderU, v1); // bottom
        draw(location, x, y + borderWidthV, borderWidthU, height - borderWidthV * 2, u0, borderV, borderU, v1 - borderV); // left
        draw(location, x + width - borderWidthU, y + borderWidthV, borderWidthU, height - borderWidthV * 2, u1 - borderU, borderV, u1, v1 - borderV); // left
        // draw body
        draw(location, x + borderWidthU, y + borderWidthV, width - borderWidthU * 2, height - borderWidthV * 2, borderU, borderV, u1 - borderU, v1 - borderV);
    }
}
