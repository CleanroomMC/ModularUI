package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.widget.sizer.Area;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

/**
 * Viewport stack
 * <p>
 * This class is responsible for calculating and keeping track of
 * embedded (into each other) scrolling areas
 */
public class GuiViewportStack implements IViewportStack {

    private static final Vector3f sharedVec = new Vector3f();
    private static final FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);

    private final Stack<TransformationMatrix> viewportStack = new Stack<>();
    private final List<Area> viewportAreas = new ArrayList<>();
    private TransformationMatrix top;
    private TransformationMatrix topViewport;

    @Override
    public void reset() {
        this.viewportStack.clear();
        this.top = null;
        this.topViewport = null;
    }

    @Override
    public Area getViewport() {
        return this.topViewport.getArea();
    }

    @Override
    public void pushViewport(IViewport viewport, Area area) {
        Matrix4f parent = this.top == null ? null : this.top.getMatrix();
        Area child = getCurrentViewportArea();
        child.set(area);
        if (this.topViewport != null) {
            if (!this.topViewport.isViewportMatrix()) {
                throw new IllegalStateException(this.topViewport.toString());
            }
            if (this.topViewport.getArea() == null) {
                throw new NullPointerException(this.topViewport.toString());
            }
            this.topViewport.getArea().clamp(child);
        }
        this.viewportStack.push(new TransformationMatrix(viewport, child, parent));
        updateViewport(false);
        this.topViewport = this.viewportStack.peek();
    }

    @Override
    public void pushMatrix() {
        this.viewportStack.push(new TransformationMatrix(this.top == null ? null : this.top.getMatrix()));
        updateViewport(false);
    }

    private Area getCurrentViewportArea() {
        while (this.viewportAreas.size() < this.viewportStack.size() + 1) {
            this.viewportAreas.add(new Area());
        }
        return this.viewportAreas.get(this.viewportStack.size());
    }

    @Override
    public void popViewport(IViewport viewport) {
        if (this.top == null || !this.top.isViewportMatrix() || this.top.getViewport() != viewport) {
            String name = this.top == null ? "null" : this.top.getViewport().toString();
            throw new IllegalStateException("Viewports must be popped in reverse order they were pushed. Tried to pop '" + viewport + "', but last pushed is '" + name + "'.");
        }
        this.viewportStack.pop();
        updateViewport(true);
    }

    @Override
    public void popMatrix() {
        if (this.top.isViewportMatrix()) {
            throw new IllegalStateException("Tried to pop viewport matrix, but at the top is a normal matrix.");
        }
        this.viewportStack.pop();
        updateViewport(false);
    }

    public void push(TransformationMatrix transformationMatrix) {
        this.viewportStack.push(new TransformationMatrix(transformationMatrix, this.top == null ? null : this.top.getMatrix()));
        updateViewport(false);
        if (this.top.isViewportMatrix()) {
            this.topViewport = this.top;
        }
    }

    public void pop(TransformationMatrix transformationMatrix) {
        if (this.top.getWrapped() != transformationMatrix) {
            throw new IllegalArgumentException();
        }
        TransformationMatrix tm = this.viewportStack.pop();
        updateViewport(tm.isViewportMatrix());
    }

    @Override
    public int getStackSize() {
        return this.viewportStack.size();
    }

    @Override
    public void popUntilIndex(int index) {
        for (int i = this.viewportStack.size() - 1; i > index; i--) {
            this.viewportStack.pop();
        }
        updateViewport(true);
    }

    @Override
    public void popUntilViewport(IViewport viewport) {
        int i = this.viewportStack.size();
        while (--i >= 0 && this.viewportStack.peek().getViewport() != viewport) {
            this.viewportStack.pop();
        }
        updateViewport(true);
    }

    public void translate(float x, float y) {
        checkViewport();
        this.top.getMatrix().translate(new Vector2f(x, y));
        this.top.markDirty();
    }

    public void translate(float x, float y, float z) {
        checkViewport();
        this.top.getMatrix().translate(vec(x, y, z));
        this.top.markDirty();
    }

    public void rotate(float angle, float x, float y, float z) {
        checkViewport();
        this.top.getMatrix().rotate(angle, vec(x, y, z));
        this.top.markDirty();
    }

    public void rotateZ(float angle) {
        checkViewport();
        this.top.getMatrix().rotate(angle, vec(0f, 0f, 1f));
        this.top.markDirty();
    }

    public void scale(float x, float y) {
        checkViewport();
        this.top.getMatrix().scale(vec(x, y, 1f));
        this.top.markDirty();
    }

    public void resetCurrent() {
        checkViewport();
        Matrix4f belowTop = this.viewportStack.size() > 1 ? this.viewportStack.get(this.viewportStack.size() - 2).getMatrix() : new Matrix4f();
        this.top.getMatrix().load(belowTop);
        this.top.markDirty();
    }

    private void checkViewport() {
        if (this.top == null) {
            throw new IllegalStateException("Tried to transform viewport, but there is no viewport!");
        }
    }

    private void updateViewport(boolean findTopViewport) {
        this.top = this.viewportStack.isEmpty() ? null : this.viewportStack.peek();
        if (!findTopViewport || this.topViewport == null || !this.topViewport.isViewportMatrix()) return;
        // find new top viewport
        this.topViewport = null;
        if (this.viewportStack.isEmpty()) return;
        ListIterator<TransformationMatrix> it = this.viewportStack.listIterator(this.viewportStack.size() - 1);
        while (it.hasPrevious()) {
            TransformationMatrix transformationMatrix1 = it.previous();
            if (transformationMatrix1.isViewportMatrix()) {
                this.topViewport = transformationMatrix1;
                break;
            }
        }
    }

    @Override
    public int transformX(float x, float y) {
        return this.top == null ? (int) x : this.top.transformX(x, y);
    }

    @Override
    public int transformY(float x, float y) {
        return this.top == null ? (int) y : this.top.transformY(x, y);
    }

    @Override
    public int unTransformX(float x, float y) {
        return this.top == null ? (int) x : this.top.unTransformX(x, y);
    }

    @Override
    public int unTransformY(float x, float y) {
        return this.top == null ? (int) y : this.top.unTransformY(x, y);
    }

    @Override
    public Vector3f transform(Vector3f vec, Vector3f dest) {
        return this.top == null ? dest.set(vec) : this.top.transform(vec, dest);
    }

    @Override
    public Vector3f unTransform(Vector3f vec, Vector3f dest) {
        return this.top == null ? dest.set(vec) : this.top.unTransform(vec, dest);
    }

    @Override
    public void applyToOpenGl() {
        if (this.top == null) return;
        this.top.getMatrix().store(floatBuffer);
        floatBuffer.position(0);
        GL11.glMultMatrix(floatBuffer);
    }

    @Nullable
    @Override
    public TransformationMatrix peek() {
        return top;
    }

    private static Vector3f vec(float x, float y, float z) {
        sharedVec.set(x, y, z);
        return sharedVec;
    }
}