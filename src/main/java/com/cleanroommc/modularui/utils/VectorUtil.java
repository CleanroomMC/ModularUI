package com.cleanroommc.modularui.utils;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class VectorUtil {

    public static final Vector3fc UNIT_X = new Vector3f(1.0f, 0.0f, 0.0f);
    public static final Vector3fc UNIT_Y = new Vector3f(0.0f, 1.0f, 0.0f);
    public static final Vector3fc UNIT_Z = new Vector3f(0.0f, 0.0f, 1.0f);

    public static Vec3d toVec3d(Vector3f vec) {
        return new Vec3d(vec.x, vec.y, vec.z);
    }

    public static Vector3f set(Vector3f target, float x, float y, float z) {
        if (target == null) target = new Vector3f();
        target.set(x, y, z);
        return target;
    }

    @NotNull
    public static Vector3f set(@Nullable Vector3f target, Vec3d vec) {
        return set(target, (float) vec.x, (float) vec.y, (float) vec.z);
    }

    @NotNull
    public static Vector3f set(@Nullable Vector3f target, Vec3i vec) {
        return set(target, vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vector3f vec3f(Vec3d vec3d) {
        return set(null, vec3d);
    }

    public static Vector3f vec3f(Vec3i vec3i) {
        return set(null, vec3i);
    }

    public static Vector3f vec3fAdd(Vector3f source, Vector3f target, float x, float y, float z) {
        if (target == null) target = new Vector3f();
        if (source == null) return set(target, x, y, z);
        if (target != source) target.set(source);
        return target.add(x, y, z);
    }

    @NotNull
    public static Vector3f vec3fAdd(Vector3f source, @Nullable Vector3f target, Vec3i vec) {
        return vec3fAdd(source, target, vec.getX(), vec.getY(), vec.getZ());
    }

    @NotNull
    public static Vector3f vec3fAdd(Vector3f source, @Nullable Vector3f target, Vec3d vec) {
        return vec3fAdd(source, target, (float) vec.x, (float) vec.y, (float) vec.z);
    }
}
