package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.util.math.BlockPos;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Projection {

    public static final Projection INSTANCE = new Projection();

    protected static final FloatBuffer MODELVIEW_MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
    protected static final FloatBuffer PROJECTION_MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
    protected static final IntBuffer VIEWPORT_BUFFER = BufferUtils.createIntBuffer(16);
    protected static final FloatBuffer PIXEL_DEPTH_BUFFER = BufferUtils.createFloatBuffer(1);
    protected static final FloatBuffer OBJECT_POS_BUFFER = BufferUtils.createFloatBuffer(3);

    private Projection() {}

    public Vector3f project(BlockPos pos) {
        // read current rendering parameters
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX_BUFFER);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_MATRIX_BUFFER);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        // rewind buffers after write by OpenGL glGet calls
        MODELVIEW_MATRIX_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();
        OBJECT_POS_BUFFER.rewind();

        // call gluProject with retrieved parameters
        GLU.gluProject(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, MODELVIEW_MATRIX_BUFFER,
                PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER, OBJECT_POS_BUFFER);

        // rewind buffers after read
        VIEWPORT_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();
        OBJECT_POS_BUFFER.rewind();

        // obtain position in Screen
        float winX = OBJECT_POS_BUFFER.get();
        float winY = OBJECT_POS_BUFFER.get();
        float winZ = OBJECT_POS_BUFFER.get();

        return new Vector3f(winX, winY, winZ);
    }

    public Vector3f unProject(int screenX, int screenY) {
        // read current rendering parameters
        GL11.glReadPixels(screenX, screenY, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, PIXEL_DEPTH_BUFFER);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW_MATRIX_BUFFER);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_MATRIX_BUFFER);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT_BUFFER);

        // rewind buffers after write by OpenGL glGet calls
        PIXEL_DEPTH_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();
        OBJECT_POS_BUFFER.rewind();

        // call gluUnProject with retrieved parameters
        GLU.gluUnProject(screenX, screenY, PIXEL_DEPTH_BUFFER.get(), MODELVIEW_MATRIX_BUFFER, PROJECTION_MATRIX_BUFFER, VIEWPORT_BUFFER,
                OBJECT_POS_BUFFER);

        // rewind buffers after read
        PIXEL_DEPTH_BUFFER.rewind();
        VIEWPORT_BUFFER.rewind();
        PROJECTION_MATRIX_BUFFER.rewind();
        MODELVIEW_MATRIX_BUFFER.rewind();
        OBJECT_POS_BUFFER.rewind();

        // obtain absolute position in world
        float posX = OBJECT_POS_BUFFER.get();
        float posY = OBJECT_POS_BUFFER.get();
        float posZ = OBJECT_POS_BUFFER.get();

        return new Vector3f(posX, posY, posZ);
    }
}
