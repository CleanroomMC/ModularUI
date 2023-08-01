package com.cleanroommc.modularui.drawable;

import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

public class TiledUITexture extends UITexture {

    private final int imageWidth, imageHeight;

    /**
     * Use {@link UITexture#builder()} with {@link Builder#tiled()}
     */
    TiledUITexture(ResourceLocation location, float u0, float v0, float u1, float v1, int imageWidth, int imageHeight, boolean canApplyTheme) {
        super(location, u0, v0, u1, v1, canApplyTheme);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public void draw(float x, float y, float width, float height) {
        if (width == this.imageWidth && height == this.imageHeight) {
            super.draw(x, y, width, height);
            return;
        }
        GuiDraw.drawTiledTexture(this.location, x, y, width, height, this.u0, this.v0, this.u1, this.v1, this.imageWidth, this.imageHeight, 0);
    }
}
