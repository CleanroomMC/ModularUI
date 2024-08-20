package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.core.Vec3i;

import org.joml.Vector3f;

public class Camera {

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

    public Camera setLookAt(Vector3f lookAt, double radius, double yaw, double pitch) {
        return setLookAt(lookAt.x, lookAt.y, lookAt.z, radius, yaw, pitch);
    }

    public Camera setLookAt(Vec3i lookAt, double radius, double yaw, double pitch) {
        return setLookAt(lookAt.getX(), lookAt.getY(), lookAt.getZ(), radius, yaw, pitch);
    }

    public Camera setLookAt(float lookAtX, float lookAtY, float lookAtZ, double radius, double yaw, double pitch) {
        setLookAt(lookAtX, lookAtY, lookAtZ);
        Vector3f pos = new Vector3f((float) Math.cos(yaw), (float) 0, (float) Math.sin(yaw));
        pos.y += (float) (Math.tan(pitch) * pos.length());
        pos.normalize().mul((float) radius);
        this.pos.set(pos.add(lookAtX, lookAtY, lookAtZ));
        return this;
    }

    public Vector3f getPos() {
        return pos;
    }

    public Vector3f getLookAt() {
        return lookAt;
    }
}
