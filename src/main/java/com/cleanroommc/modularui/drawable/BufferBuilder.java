package com.cleanroommc.modularui.drawable;

import net.minecraft.client.renderer.Tessellator;

/**
 * Simple wrapper to keep code the same as 1.12 as much as possible
 */
public class BufferBuilder {

    private boolean isPosSet = false, isTexSet = false, isColorSet = false;

    private double x, y, z;
    private double u, v;
    private int r, g, b, a;

    public static final BufferBuilder buffer = new BufferBuilder();
    public static final BufferBuilder bufferbuilder = buffer;

    private BufferBuilder() {}

    public BufferBuilder pos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        isPosSet = true;
        return this;
    }

    public BufferBuilder tex(double u, double v) {
        this.u = u;
        this.v = v;
        isTexSet = true;
        return this;
    }

    public BufferBuilder color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        isColorSet = true;
        return this;
    }

    public BufferBuilder color(float r, float g, float b, float a) {
        return color((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F), (int) (a * 255.0F));
    }

    public void endVertex() {
        if (isColorSet) {
            Tessellator.instance.setColorRGBA(r, g, b, a);
            isColorSet = false;
        }
        if (isTexSet) {
            Tessellator.instance.setTextureUV(u, v);
            isTexSet = false;
        }
        if (isPosSet) {
            Tessellator.instance.addVertex(x, y, z);
            isPosSet = false;
        }
    }
}
