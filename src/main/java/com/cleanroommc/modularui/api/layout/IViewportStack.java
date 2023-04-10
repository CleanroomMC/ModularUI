package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.screen.viewport.TransformationMatrix;
import com.cleanroommc.modularui.widget.sizer.Area;
import org.lwjgl.util.vector.Vector3f;

/**
 * This handles all viewports in a GUI.
 */
public interface IViewportStack {

    /**
     * Reset all viewports.
     */
    void reset();

    /**
     * @return current viewport
     */
    Area getViewport();

    void pushViewport(IViewport viewport, Area area);

    void pushMatrix();

    void popViewport(IViewport viewport);

    void popMatrix();

    int getCurrentViewportIndex();

    void popUntilIndex(int index);

    void popUntilViewport(IViewport viewport);

    void translate(float x, float y);

    void translate(float x, float y, float z);

    void rotate(float angle, float x, float y, float z);

    void rotateZ(float angle);

    void scale(float x, float y);

    void resetCurrent();

    int transformX(float x, float y);

    int transformY(float x, float y);

    int unTransformX(float x, float y);

    int unTransformY(float x, float y);

    default Vector3f transform(Vector3f vec) {
        return transform(vec, vec);
    }

    Vector3f transform(Vector3f vec, Vector3f dest);

    default Vector3f unTransform(Vector3f vec) {
        return unTransform(vec, vec);
    }

    Vector3f unTransform(Vector3f vec, Vector3f dest);

    void applyToOpenGl();

    TransformationMatrix peek();
}