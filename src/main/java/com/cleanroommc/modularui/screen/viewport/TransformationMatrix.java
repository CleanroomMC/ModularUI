package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.utils.Matrix4f;
import com.cleanroommc.modularui.utils.Vector3f;
import com.cleanroommc.modularui.widget.sizer.Area;

import org.jetbrains.annotations.Nullable;

/**
 * A single matrix in a matrix stack. Also has some other information.
 */
public class TransformationMatrix {

    public static final TransformationMatrix EMPTY = new TransformationMatrix(null);

    private final TransformationMatrix wrapped;
    private final IViewport viewport;
    private final Area area;
    private final Matrix4f matrix;
    private final Matrix4f invertedMatrix = new Matrix4f();

    private final boolean viewportMatrix;
    private boolean dirty = true;

    public TransformationMatrix(TransformationMatrix parent, @Nullable Matrix4f parentMatrix) {
        this.wrapped = parent;
        this.viewport = parent.viewport;
        this.area = parent.area;
        this.matrix = parentMatrix == null ? new Matrix4f(parent.getMatrix()) : Matrix4f.mul(parentMatrix, parent.getMatrix(), null);
        this.viewportMatrix = parent.viewportMatrix;
    }

    public TransformationMatrix(@Nullable Matrix4f parent) {
        this.wrapped = null;
        this.viewport = null;
        this.area = null;
        this.matrix = new Matrix4f();
        this.viewportMatrix = false;
        if (parent != null) {
            this.matrix.load(parent);
        }
    }

    public TransformationMatrix(IViewport viewport, Area area, @Nullable Matrix4f parent) {
        this.wrapped = null;
        this.viewport = viewport;
        this.area = area;
        this.matrix = new Matrix4f();
        this.viewportMatrix = true;
        if (parent != null) {
            this.matrix.load(parent);
        }
    }

    public TransformationMatrix getWrapped() {
        return this.wrapped;
    }

    public IViewport getViewport() {
        return this.viewport;
    }

    public Area getArea() {
        return this.area;
    }

    public Matrix4f getMatrix() {
        return this.matrix;
    }

    public Matrix4f getInvertedMatrix() {
        if (this.dirty) {
            if (Matrix4f.invert(this.matrix, this.invertedMatrix) == null) {
                this.invertedMatrix.load(this.matrix);
            }
            this.dirty = false;
        }
        return this.invertedMatrix;
    }

    public boolean isViewportMatrix() {
        return this.viewportMatrix;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public int transformX(float x, float y) {
        Matrix4f m = getMatrix();
        return (int) (x * m.m00 + y * m.m10 + m.m30);
    }

    public int transformY(float x, float y) {
        Matrix4f m = getMatrix();
        return (int) (x * m.m01 + y * m.m11 + m.m31);
    }

    public int unTransformX(float x, float y) {
        Matrix4f m = getInvertedMatrix();
        return (int) (x * m.m00 + y * m.m10 + m.m30);
    }

    public int unTransformY(float x, float y) {
        Matrix4f m = getInvertedMatrix();
        return (int) (x * m.m01 + y * m.m11 + m.m31);
    }

    public Vector3f transform(Vector3f vec, Vector3f dest) {
        return transform(getMatrix(), vec, dest);
    }

    public Vector3f unTransform(Vector3f vec, Vector3f dest) {
        return transform(getInvertedMatrix(), vec, dest);
    }

    public static Vector3f transform(Matrix4f m, Vector3f vec, Vector3f dest) {
        float x = m.m00 * vec.x + m.m10 * vec.y + m.m20 * vec.z + m.m30;
        float y = m.m01 * vec.x + m.m11 * vec.y + m.m21 * vec.z + m.m31;
        float z = m.m02 * vec.x + m.m12 * vec.y + m.m22 * vec.z + m.m32;
        dest.set(x, y, z);
        return dest;
    }
}
