package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.widget.sizer.Area;
import net.minecraft.client.renderer.GlStateManager;

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

    /**
     * Pushes a new viewport
     *
     * @param area area of the viewport
     */
    void pushViewport(Area area);

    /**
     * Pops the current viewport from the stack
     */
    void popViewport();

    /**
     * The current total shift in x
     */
    int getShiftX();

    /**
     * The current total shift in y
     */
    int getShiftY();

    /**
     * Get global X (relative to root element/screen)
     */
    int globalX(int x);

    /**
     * Get global Y (relative to root element/screen)
     */
    int globalY(int y);

    /**
     * Get current local X (relative to current viewport)
     */
    int localX(int x);

    /**
     * Get current local Y (relative to current viewport)
     */
    int localY(int y);

    /**
     * Applies a shift transformation in x
     *
     * @param x shift amount in x
     */
    void shiftX(int x);

    /**
     * Applies a shift transformation in y
     *
     * @param y shift amount in y
     */
    void shiftY(int y);

    /**
     * Applies the active transformation to open gl
     */
    default void applyToOpenGl() {
        GlStateManager.translate(-getShiftX(), -getShiftY(), 0);
    }

    /**
     * Removes the active transformation to open gl
     */
    default void unapplyToOpenGl() {
        GlStateManager.translate(getShiftX(), getShiftY(), 0);
    }
}