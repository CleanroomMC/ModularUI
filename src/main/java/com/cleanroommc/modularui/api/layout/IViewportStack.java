package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.widget.sizer.Area;

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

    void shiftX(int x);

    void shiftY(int y);
}