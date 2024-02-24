package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.utils.GuiUtils;

import net.minecraft.client.renderer.GlStateManager;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Highly experimental
 */
@ApiStatus.Experimental
public class Plane3D {

    private float w = 480, h = 270;
    private float scale = 1f;
    private float aX = 0.5f, aY = 0.5f;
    private Vec3d normal = Direction.NORTH.asVec3d();
    private Vec3d rotation = this.normal;


    public void transform() {
        transform(this.normal, Direction.ORIGIN.asVec3d(), this.normal.scale(-1));
    }

    public void transform(Vec3d target, Vec3d orig, Vec3d looking) {
        // translate to anchor
        GlStateManager.translate(-this.w * this.aX, -this.h * this.aY, 0);
        // translate for scale and rotation
        GlStateManager.translate(this.w / 2f, this.h / 2f, 0);
        // scale to size. 0.0625 is 1/16
        GlStateManager.scale(0.0625 * this.scale, 0.0625 * this.scale, 0.0625 * this.scale);
        // rotate 180 deg
        GlStateManager.rotate(180, 0, 0, 1);

        // apply facing direction
        // handle yaw (left-right)
        Vec3d diff = orig.subtract(target);
        double yaw = Math.atan(diff.z / diff.x);
        if (diff.x < 0) yaw += (Math.PI / 2);
        else yaw -= (Math.PI / 2);

        // handle pitch (up-down)
        Vec3d vec = new Vec3d(looking.x, 0, looking.z);
        double pitch = Math.atan(looking.y / vec.length());
        this.rotation = new Vec3d(pitch, yaw, 0);

        Matrix4f mYaw = new Matrix4f()
                .rotate((float) yaw, Direction.UP.asVector3f());
        Matrix4f mPitch = new Matrix4f()
                .rotate((float) pitch, Direction.EAST.asVector3f());

        GuiUtils.applyTransformationMatrix(mYaw);
        GuiUtils.applyTransformationMatrix(mPitch);
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
        this.normal = new Vec3d(x, y, z).normalize();
    }

    public Vec3d getRotation() {
        return this.rotation;
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

    private enum Direction {
        ORIGIN(0, 0, 0),
        UP(0, 1, 0),
        DOWN(0, -1, 0),
        NORTH(0, 0, -1),
        SOUTH(0, 0, 1),
        EAST(1, 0, 0),
        WEST(-1, 0, 0);

        private final Vector3f vector3f;
        private final Vec3d vec3d;
        Direction(float x, float y, float z) {
            vector3f = new Vector3f(x, y, z);
            vec3d = new Vec3d(x, y, z);
        }

        Vector3f asVector3f() {
            return vector3f;
        }

        Vec3d asVec3d() {
            return vec3d;
        }
    }
}
