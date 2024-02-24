package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.utils.GuiUtils;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;

/**
 * Highly experimental
 */
@ApiStatus.Experimental
public class Plane3D {

    private float w = 480, h = 270;
    private float scale = 1f;
    private float aX = 0.5f, aY = 0.5f;
    private float nX = 0, nY = 0, nZ = 1;
    private final Quaternion facer = new Quaternion();

    public void transformRectangle() {
        // translate to anchor
        GlStateManager.translate(-this.w * this.aX, -this.h * this.aY, 0);
        // translate for scale and rotation
        GlStateManager.translate(this.w / 2f, this.h / 2f, 0);
        // scale to size. 0.0625 is 1/16
        GlStateManager.scale(0.0625 * this.scale, 0.0625 * this.scale, 0.0625 * this.scale);
        // rotate 180 deg
        GlStateManager.rotate(180, 0, 0, 1);
        // apply facing direction
        if (this.nX != 0 || this.nY != 0 || this.nZ != 1) {
            Matrix4f rotation = new Matrix4f();
            rotation.m00 = -this.nZ + (this.nY * this.nY * (1 + this.nZ)) / (this.nX * this.nX + this.nY * this.nY);
            rotation.m10 = -(this.nX * this.nY * (1 + this.nZ)) / (this.nX * this.nX + this.nY * this.nY);
            rotation.m20 = this.nX;
            rotation.m01 = -(this.nX * this.nY * (1 + this.nZ)) / (this.nX * this.nX + this.nY * this.nY);
            rotation.m11 = -this.nZ + (this.nX * this.nX * (1 + this.nZ)) / (this.nX * this.nX + this.nY * this.nY);
            rotation.m21 = this.nY;
            rotation.m02 = -this.nX;
            rotation.m12 = -this.nY;
            rotation.m22 = -this.nZ;
            GuiUtils.applyTransformationMatrix(rotation);
        }
        // un-translate for scale and rotation
        GlStateManager.translate(-(this.w / 2f), -(this.h / 2f), 0);
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

    public void setTarget(Entity entity) {
        BlockPos pos = entity.getPosition();
        this.facer.x = pos.getX();
        this.facer.y = pos.getY();
        this.facer.z = pos.getZ();
        this.facer.w = 1;
    }

    public void setAnchor(float x, float y) {
        this.aX = x;
        this.aY = y;
    }

    public float getWidth() {
        return this.w;
    }

    public float getHeight() {
        return this.h;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return this.scale;
    }
}
