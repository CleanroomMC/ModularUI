package com.cleanroommc.modularui.drawable;

import net.minecraft.client.renderer.Tessellator;

/**
 * Simple wrapper to keep code the same as 1.12 as much as possible
 */
public class BufferBuilder {

    public static final BufferBuilder buffer = new BufferBuilder();
    public static final BufferBuilder bufferbuilder = buffer;

    private BufferBuilder() {}

    public BufferBuilder pos(double x, double y, double z) {
        Tessellator.instance.addVertex(x, y, z);
        return this;
    }

    public BufferBuilder tex(double u, double v) {
        Tessellator.instance.setTextureUV(u, v);
        return this;
    }

    public BufferBuilder color(float r, float g, float b, float a) {
        Tessellator.instance.setColorRGBA_F(r, g, b, a);
        return this;
    }

    public void endVertex() {}
}
