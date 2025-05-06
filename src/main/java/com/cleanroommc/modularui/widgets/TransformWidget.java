package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.Matrix4f;
import com.cleanroommc.modularui.utils.Vector3f;
import com.cleanroommc.modularui.widget.DelegatingSingleChildWidget;

import java.util.function.Consumer;

public class TransformWidget extends DelegatingSingleChildWidget<TransformWidget> {

    private static final Vector3f sharedVec = new Vector3f();

    private final Matrix4f constTransform = new Matrix4f();
    private boolean hasConstTransform = false;
    private Consumer<IViewportStack> transform;

    public TransformWidget() {}

    public TransformWidget(IWidget child) {
        child(child);
    }

    @Override
    public void transform(IViewportStack stack) {
        super.transform(stack);
        if (this.hasConstTransform) stack.multiply(this.constTransform);
        if (this.transform != null) this.transform.accept(stack);
    }

    public TransformWidget transform(Consumer<IViewportStack> transform) {
        this.transform = transform;
        return this;
    }

    public TransformWidget translate(float x, float y) {
        this.hasConstTransform = true;
        this.constTransform.translate(x, y);
        return this;
    }

    public TransformWidget rotate(float angle, float x, float y, float z) {
        this.hasConstTransform = true;
        this.constTransform.rotate(angle, vec(x, y, z));
        return this;
    }

    public TransformWidget scale(float x, float y) {
        this.hasConstTransform = true;
        this.constTransform.scale(vec(x, y, 1));
        return this;
    }

    private static Vector3f vec(float x, float y, float z) {
        sharedVec.set(x, y, z);
        return sharedVec;
    }
}
