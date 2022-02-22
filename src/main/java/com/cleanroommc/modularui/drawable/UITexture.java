package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.math.GuiArea;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class UITexture implements IDrawable {

    public final ResourceLocation location;
    public final float u0, v0, u1, v1;

    /**
     * Creates a drawable texture
     *
     * @param location location of the texture
     * @param u0       x offset of the image (0-1)
     * @param v0       y offset of the image (0-1)
     * @param u1       x end offset of the image (0-1)
     * @param v1       y end offset of the image (0-1)
     */
    public UITexture(ResourceLocation location, float u0, float v0, float u1, float v1) {
        if (!location.getPath().endsWith(".png")) {
            location = new ResourceLocation(location.getNamespace(), location.getPath() + ".png");
        }
        if (!location.getPath().startsWith("textures/")) {
            location = new ResourceLocation(location.getNamespace(), "textures/" + location.getPath());
        }
        this.location = location;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
    }

    public static UITexture fullImage(ResourceLocation location) {
        return new UITexture(location, 0, 0, 1, 1);
    }

    public static UITexture fullImage(String location) {
        return fullImage(new ResourceLocation(location));
    }

    public static UITexture fullImage(String mod, String location) {
        return fullImage(new ResourceLocation(mod, location));
    }

    public static UITexture partly(ResourceLocation location, int imageWidth, int imageHeight, int u0, int v0, int u1, int v1) {
        return new UITexture(location, u0 / (float) imageWidth, v0 / (float) imageHeight, u1 / (float) imageWidth, v1 / (float) imageHeight);
    }

    public static UITexture partly(String location, int imageWidth, int imageHeight, int u0, int v0, int u1, int v1) {
        return partly(new ResourceLocation(location), imageWidth, imageHeight, u0, v0, u1, v1);
    }

    public static UITexture partly(String domain, String location, int imageWidth, int imageHeight, int u0, int v0, int u1, int v1) {
        return partly(new ResourceLocation(domain, location), imageWidth, imageHeight, u0, v0, u1, v1);
    }

    public UITexture getSubArea(GuiArea bounds) {
        return getSubArea(bounds.x0, bounds.y0, bounds.x1, bounds.y1);
    }

    public UITexture getSubArea(Pos2d pos, Size size) {
        return getSubArea(pos.x, pos.y, pos.x + size.width, pos.y + size.height);
    }

    public UITexture getSubArea(float u0, float v0, float u1, float v1) {
        return new UITexture(location, calcUV0(this.u0, u0), calcUV0(this.v0, v0), this.u1 * u1, this.v1 * v1);
    }

    public ResourceLocation getLocation() {
        return location;
    }

    private float calcUV0(float oldV, float newV) {
        return oldV == 0.0F ? oldV + newV : oldV + oldV * newV;
    }

    @Override
    public void draw(Pos2d pos, Size size, float partialTicks) {
        float x0 = pos.x, y0 = pos.y, x1 = x0 + size.width, y1 = y0 + size.height;
        Minecraft.getMinecraft().renderEngine.bindTexture(location);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x0, y1, 0.0f).tex(u0, v1).endVertex();
        bufferbuilder.pos(x1, y1, 0.0D).tex(u1, v1).endVertex();
        bufferbuilder.pos(x1, y0, 0.0D).tex(u1, v0).endVertex();
        bufferbuilder.pos(x0, y0, 0.0D).tex(u0, v0).endVertex();
        tessellator.draw();
    }
}
