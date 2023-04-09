package com.cleanroommc.modularui.drawable;

import net.minecraft.client.renderer.Tessellator;

/**
 * Simple wrapper to keep code the same as 1.12 as much as possible
 */
public class BufferBuilder {

    private Double x, y, z;
    private Double u, v;
    private Integer r, g, b, a;

    public static final BufferBuilder buffer = new BufferBuilder();
    public static final BufferBuilder bufferbuilder = buffer;

    private BufferBuilder() {}

    public BufferBuilder pos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public BufferBuilder tex(double u, double v) {
        this.u = u;
        this.v = v;
        return this;
    }

    public BufferBuilder color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }

    public BufferBuilder color(float r, float g, float b, float a) {
        return color((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F), (int) (a * 255.0F));
    }

    public void endVertex() {
        if (r != null) {
            Tessellator.instance.setColorRGBA(r, g, b, a);
            r = g = b = a = null;
        }
        if (u != null) {
            Tessellator.instance.setTextureUV(u, v);
            u = v = null;
        }
        if (x != null) {
            Tessellator.instance.addVertex(x, y, z);
            x = y = z = null;
        }
    }
}
