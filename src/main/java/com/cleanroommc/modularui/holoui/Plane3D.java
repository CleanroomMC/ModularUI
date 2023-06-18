package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.utils.GuiUtils;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

/**
 * Highly experimental
 */
@ApiStatus.Experimental
public class Plane3D {

    private float w = 480, h = 270;
    private float scale = 1f;
    private float aX = 0.5f, aY = 0.5f;
    private float nX = 0, nY = 0, nZ = 1;

    public void transformRectangle() {
        // translate to anchor
        GlStateManager.translate(-w * aX, -h * aY, 0);
        // translate for scale and rotation
        GlStateManager.translate(w / 2f, h / 2f, 0);
        // scale to size. 0.0625 is 1/16
        GlStateManager.scale(0.0625 * this.scale, 0.0625 * this.scale, 0.0625 * this.scale);
        // rotate 180 deg
        GlStateManager.rotate(180, 0, 0, 1);
        // apply facing direction
        if (nX != 0 || nY != 0 || nZ != 1) {
            Matrix4f rotation = new Matrix4f();
            rotation.m00 = -nZ + (nY * nY * (1 + nZ)) / (nX * nX + nY * nY);
            rotation.m10 = -(nX * nY * (1 + nZ)) / (nX * nX + nY * nY);
            rotation.m20 = nX;
            rotation.m01 = -(nX * nY * (1 + nZ)) / (nX * nX + nY * nY);
            rotation.m11 = -nZ + (nX * nX * (1 + nZ)) / (nX * nX + nY * nY);
            rotation.m21 = nY;
            rotation.m02 = -nX;
            rotation.m12 = -nY;
            rotation.m22 = -nZ;
            GuiUtils.applyTransformationMatrix(rotation);
        }
        // un-translate for scale and rotation
        GlStateManager.translate(-(w / 2f), -(h / 2f), 0);
    }

    public void setSize(float w, float h) {
        this.w = w;
        this.h = h;
    }

    public void setWidthWithProp(float w) {
        float factor = w / this.w;
        this.w = w;
        this.h *= factor;
    }

    public void setHeightWithProp(float h) {
        float factor = h / this.h;
        this.w *= factor;
        this.h = h;
    }

    public void setNormal(float x, float y, float z) {
        float square = x * x + y * y + z * z;
        if (square != 1) {
            float factor = (float) Math.sqrt(square);
            x /= factor;
            y /= factor;
            z /= factor;
        }
        this.nX = x;
        this.nY = y;
        this.nZ = z;
    }

    public void setAnchor(float x, float y) {
        this.aX = x;
        this.aY = y;
    }

    public float getWidth() {
        return w;
    }

    public float getHeight() {
        return h;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }
}
