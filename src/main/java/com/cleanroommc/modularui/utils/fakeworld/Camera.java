package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.util.math.Vec3i;

import org.lwjgl.util.vector.Vector3f;

public class Camera {

    public static final double PI2 = 2 * Math.PI;

    private final Vector3f pos;
    private final Vector3f lookAt;

    public Camera(Vector3f pos, Vector3f lookAt) {
        this.pos = pos;
        this.lookAt = lookAt;
    }

    public Camera setLookAt(Vector3f pos, Vector3f lookAt) {
        this.pos.set(pos);
        this.lookAt.set(lookAt);
        return this;
    }

    public Camera setLookAt(float x, float y, float z) {
        this.lookAt.set(x, y, z);
        return this;
    }

    public Camera setPos(float x, float y, float z) {
        this.lookAt.set(x, y, z);
        return this;
    }

    public Camera setLookAt(Vector3f lookAt, double radius, double rotationPitch, double rotationYaw) {
        return setLookAt(lookAt.x, lookAt.y, lookAt.z, radius, rotationPitch, rotationYaw);
    }

    public Camera setLookAt(Vec3i lookAt, double radius, double rotationPitch, double rotationYaw) {
        return setLookAt(lookAt.getX(), lookAt.getY(), lookAt.getZ(), radius, rotationPitch, rotationYaw);
    }

    public Camera setLookAt(float lookAtX, float lookAtY, float lookAtZ, double radius, double rotationPitch, double rotationYaw) {
        setLookAt(lookAtX, lookAtY, lookAtZ);
        Vector3f pos = new Vector3f((float) Math.cos(rotationPitch), (float) 0, (float) Math.sin(rotationPitch));
        pos.y += (float) (Math.tan(rotationYaw) * pos.length());
        pos.normalise().scale((float) radius);
        this.pos.set(pos.translate(lookAtX, lookAtY, lookAtZ));
        return this;
    }

    public Vector3f getPos() {
        return pos;
    }

    public Vector3f getLookAt() {
        return lookAt;
    }
}
