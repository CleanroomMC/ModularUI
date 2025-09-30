package com.cleanroommc.modularui.utils.fakeworld;

import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.utils.Vector3f;

import net.minecraft.util.math.Vec3i;

import org.jetbrains.annotations.Nullable;

public class Camera {

    private final Vector3f pos = new Vector3f();
    private final Vector3f lookAt = new Vector3f();
    private final Vector3f temp = new Vector3f();
    private float yaw, pitch, dist;

    public Camera() {}

    public Camera setPosAndLookAt(Vector3f pos, Vector3f lookAt) {
        return setPosAndLookAt(pos.x, pos.y, pos.z, lookAt.x, lookAt.y, lookAt.z);
    }

    public Camera setPosAndLookAt(float xPos, float yPos, float zPos, Vector3f lookAt) {
        return setPosAndLookAt(xPos, yPos, zPos, lookAt.x, lookAt.y, lookAt.z);
    }

    public Camera setPosAndLookAt(Vector3f pos, float xLook, float yLook, float zLook) {
        return setPosAndLookAt(pos.x, pos.y, pos.z, xLook, yLook, zLook);
    }

    public Camera setPosAndLookAt(float xPos, float yPos, float zPos, float xLook, float yLook, float zLook) {
        this.pos.set(xPos, yPos, zPos);
        this.lookAt.set(xLook, yLook, zLook);
        Vector3f look = getLookVec(this.temp);
        Vector3f.resetUnitVectors();
        this.pitch = Vector3f.angle(look, Vector3f.UNIT_Y) + MathUtils.PI_HALF;
        look.setY(0);
        this.yaw = Vector3f.angle(look, Vector3f.UNIT_X);
        this.dist = this.pos.distanceTo(this.lookAt);
        return this;
    }

    public Camera setLookAtKeepPos(float x, float y, float z) {
        setPosAndLookAt(this.pos, x, y, z);
        return this;
    }

    public Camera setLookAtKeepAngle(float x, float y, float z) {
        // just calculate the offset
        this.temp.set(x, y, z);
        Vector3f.sub(this.temp, this.lookAt, this.temp);
        this.pos.add(this.temp);
        this.lookAt.set(this.temp);
        return this;
    }

    public Camera setPosKeepLookAt(float x, float y, float z) {
        return setPosAndLookAt(x, y, z, this.lookAt);
    }

    public Camera setPosKeepAngle(float x, float y, float z) {
        // just calculate the offset
        this.temp.set(x, y, z);
        Vector3f.sub(this.temp, this.pos, this.temp);
        this.lookAt.add(this.temp);
        this.pos.set(this.temp);
        return this;
    }

    public Camera setAngleKeepLookAt(float radius, float yaw, float pitch) {
        return setLookAtAndAngle(this.lookAt, radius, yaw, pitch);
    }

    public Camera setLookAtAndAngle(Vector3f lookAt, float radius, float yaw, float pitch) {
        return setLookAtAndAngle(lookAt.x, lookAt.y, lookAt.z, radius, yaw, pitch);
    }

    public Camera setLookAtAndAngle(Vec3i lookAt, float radius, float yaw, float pitch) {
        return setLookAtAndAngle(lookAt.getX(), lookAt.getY(), lookAt.getZ(), radius, yaw, pitch);
    }

    public Camera setLookAtAndAngle(float lookAtX, float lookAtY, float lookAtZ, float dist, float yaw, float pitch) {
        this.lookAt.set(lookAtX, lookAtY, lookAtZ);
        this.yaw = yaw;
        this.pitch = pitch;
        this.dist = dist;
        Vector3f v = this.temp;
        v.set(MathUtils.cos(yaw), 0, MathUtils.sin(yaw));
        v.y = MathUtils.tan(pitch) * v.length();
        v.normalise().scale(dist);
        this.pos.set(v.translate(lookAtX, lookAtY, lookAtZ));
        return this;
    }

    public Camera setPosAndAngle(float posX, float posY, float posZ, float dist, float yaw, float pitch) {
        this.pos.set(posX, posY, posZ);
        this.yaw = yaw;
        this.pitch = pitch;
        this.dist = dist;
        Vector3f v = this.temp;
        v.set(MathUtils.cos(MathUtils.PI_HALF - yaw), 0, MathUtils.sin(MathUtils.PI_HALF - yaw));
        v.y = MathUtils.tan(MathUtils.PI_HALF - pitch) * v.length();
        v.normalise().scale(dist);
        this.lookAt.set(v).add(this.pos);
        return this;
    }

    public void setDistanceKeepLookAt(float dist) {
        if (dist == this.dist) return;
        this.dist = dist;
        Vector3f.sub(this.pos, this.lookAt, this.temp);
        this.temp.normalise().scale(dist);
        Vector3f.add(this.lookAt, this.temp, this.pos);
    }

    public void scaleDistanceKeepLookAt(float dist) {
        if (dist == 1) return;
        this.dist *= dist;
        Vector3f.sub(this.pos, this.lookAt, this.temp);
        this.temp.scale(dist);
        Vector3f.add(this.lookAt, this.temp, this.pos);
    }

    public Vector3f getPos() {
        return pos.copy();
    }

    public Vector3f getLookAt() {
        return lookAt.copy();
    }

    public Vector3f getLookVec() {
        return getLookVec(null);
    }

    public Vector3f getLookVec(@Nullable Vector3f dest) {
        return Vector3f.sub(lookAt, pos, dest);
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getDist() {
        return dist;
    }
}
