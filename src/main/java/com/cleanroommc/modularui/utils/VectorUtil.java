package com.cleanroommc.modularui.utils;

import net.minecraft.core.Vec3i;

import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import net.minecraft.util.Mth.*;

public class VectorUtil {

    public static Vector3f set(Vector3f target, float x, float y, float z) {
        if (target == null) target = new Vector3f();
        target.set(x, y, z);
        return target;
    }

    @NotNull
    public static Vector3f set(@Nullable Vector3f target, Vec3 vec) {
        return set(target, (float) vec.x, (float) vec.y, (float) vec.z);
    }

    @NotNull
    public static Vector3f set(@Nullable Vector3f target, Vec3i vec) {
        return set(target, vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vector3f vec3f(Vec3 vec3d) {
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
    public static Vector3f vec3fAdd(Vector3f source, @Nullable Vector3f target, Vec3 vec) {
        return vec3fAdd(source, target, (float) vec.x, (float) vec.y, (float) vec.z);
    }
}
