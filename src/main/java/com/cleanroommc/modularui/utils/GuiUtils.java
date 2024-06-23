package com.cleanroommc.modularui.utils;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

public class GuiUtils {

    private static final FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);

    public static FloatBuffer getTransformationBuffer() {
        return getTransformationBuffer(BufferUtils.createFloatBuffer(16));
    }

    public static FloatBuffer getTransformationBuffer(FloatBuffer floatBuffer) {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, floatBuffer);
        floatBuffer.position(0);
        return floatBuffer;
    }

    public static Matrix4f getTransformationMatrix() {
        return getTransformationMatrix(new Matrix4f());
    }

    public static Matrix4f getTransformationMatrix(Matrix4f matrix4f) {
        floatBuffer.rewind();
        getTransformationBuffer(floatBuffer);
        matrix4f.load(floatBuffer);
        return matrix4f;
    }

    public static void setTransformationMatrix(Matrix4f matrix) {
        floatBuffer.rewind();
        matrix.store(floatBuffer);
        floatBuffer.rewind();
        GL11.glLoadMatrix(floatBuffer);
    }

    public static void applyTransformationMatrix(Matrix4f matrix) {
        floatBuffer.rewind();
        matrix.store(floatBuffer);
        floatBuffer.rewind();
        GL11.glMultMatrix(floatBuffer);
    }
}
