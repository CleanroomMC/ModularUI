package com.cleanroommc.modularui.screen.viewport;

public class LocatedElement<T> {

    private final T element;
    private final TransformationMatrix transformationMatrix;

    public LocatedElement(T element, TransformationMatrix transformationMatrix) {
        this.element = element;
        this.transformationMatrix = new TransformationMatrix(transformationMatrix, null);
    }

    public T getElement() {
        return this.element;
    }

    public TransformationMatrix getTransformationMatrix() {
        return this.transformationMatrix;
    }

    public void applyMatrix(GuiContext context) {
        context.push(this.transformationMatrix);
    }

    public void unapplyMatrix(GuiContext context) {
        context.pop(this.transformationMatrix);
    }

    @Override
    public String toString() {
        return "LocatedElement[" + getElement() + "]";
    }
}
