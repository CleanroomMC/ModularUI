package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.widget.sizer.Area;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Viewport stack
 * <p>
 * This class is responsible for calculating and keeping track of
 * embedded (into each other) scrolling areas
 */
public class GuiViewportStack implements IViewportStack {

    private final Stack<IViewport> viewportStack = new Stack<>();
    private final Stack<Area> viewportAreaStack = new Stack<>();
    private final List<Area> viewportAreas = new ArrayList<>();
    private int shiftX = 0;
    private int shiftY = 0;

    @Override
    public void reset() {
        this.shiftX = 0;
        this.shiftY = 0;

        this.viewportAreaStack.clear();
    }

    @Override
    public Area getViewport() {
        return this.viewportAreaStack.peek();
    }

    @Override
    public void pushViewport(IViewport viewport, Area area) {
        if (this.viewportAreaStack.isEmpty()) {
            Area child = this.getCurrentViewportArea();

            child.set(area);
            this.viewportAreaStack.push(child);
        } else {
            Area current = this.viewportAreaStack.peek();
            Area child = this.getCurrentViewportArea();

            child.set(area);
            current.clamp(child);
            this.viewportAreaStack.push(child);
        }
        this.viewportStack.push(viewport);
    }

    private Area getCurrentViewportArea() {
        while (this.viewportAreas.size() < this.viewportAreaStack.size() + 1) {
            this.viewportAreas.add(new Area());
        }

        return this.viewportAreas.get(this.viewportAreaStack.size());
    }

    @Override
    public void popViewport(IViewport viewport) {
        if (this.viewportStack.peek() != viewport) {
            throw new IllegalStateException("Viewports must be popped in reverse order they were pushed. Tried to pop '" + viewport + "', but last pushed is '" + this.viewportStack.peek() + "'.");
        }
        this.viewportStack.pop();
        this.viewportAreaStack.pop();
    }

    @Override
    public int getCurrentViewportIndex() {
        return this.viewportStack.size();
    }

    @Override
    public void popUntilIndex(int index) {
        for (int i = this.viewportStack.size() - 1; i > index; i--) {
            this.viewportStack.pop();
            this.viewportAreaStack.pop();
        }
    }

    @Override
    public void popUntilViewport(IViewport viewport) {
        int i = this.viewportStack.size();
        while (--i >= 0 && this.viewportStack.peek() != viewport) {
            this.viewportStack.pop();
            this.viewportAreaStack.pop();
        }
    }

    @Override
    public int getShiftX() {
        return this.shiftX;
    }

    @Override
    public int getShiftY() {
        return this.shiftY;
    }

    /**
     * Get global X (relative to root element/screen)
     */
    @Override
    public int globalX(int x) {
        return x - this.shiftX;
    }

    /**
     * Get global Y (relative to root element/screen)
     */
    @Override
    public int globalY(int y) {
        return y - this.shiftY;
    }

    /**
     * Get current local X (relative to current viewport)
     */
    @Override
    public int localX(int x) {
        return x + this.shiftX;
    }

    /**
     * Get current local Y (relative to current viewport)
     */
    @Override
    public int localY(int y) {
        return y + this.shiftY;
    }

    @Override
    public void shiftX(int x) {
        this.shiftX += x;

        if (!this.viewportAreaStack.isEmpty()) {
            this.viewportAreaStack.peek().x += x;
        }
    }

    @Override
    public void shiftY(int y) {
        this.shiftY += y;

        if (!this.viewportAreaStack.isEmpty()) {
            this.viewportAreaStack.peek().y += y;
        }
    }
}