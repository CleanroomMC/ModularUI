package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.widget.sizer.Area;

import java.util.Iterator;

/**
 * General interface for viewport stack
 */
public interface IViewportStack {

    void reset();

    Area getViewport();

    void pushViewport(Area area);

    void popViewport();

    int getShiftX();

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