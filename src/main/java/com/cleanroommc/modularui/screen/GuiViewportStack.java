package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.api.IViewport;
import com.cleanroommc.modularui.api.IViewportStack;
import com.cleanroommc.modularui.widget.sizer.Area;

import java.util.*;

/**
 * Viewport stack
 * <p>
 * This class is responsible for calculating and keeping track of
 * embedded (into each other) scrolling areas
 */
public class GuiViewportStack implements IViewportStack {

    private final Stack<Area> viewportStack = new Stack<>();
    private final List<Area> viewportAreas = new ArrayList<>();
    private int shiftX = 0;
    private int shiftY = 0;

    public static GuiViewportStack fromElement(IWidget element) {
        GuiViewportStack stack = new GuiViewportStack();

        stack.applyFromElement(element);

        return stack;
    }

    private void applyFromElement(IWidget element) {
        List<IViewport> elements = new ArrayList<>();

        while (element != null) {
            if (element instanceof IViewport) {
                elements.add((IViewport) element);
            }

            element = element.getParent();
        }

        for (int i = elements.size() - 1; i >= 0; i--) {
            elements.get(i).apply(this);
        }
    }

    @Override
    public void reset() {
        this.shiftX = 0;
        this.shiftY = 0;

        this.viewportStack.clear();
    }

    @Override
    public Area getViewport() {
        return this.viewportStack.peek();
    }

    @Override
    public void pushViewport(Area area) {
        if (this.viewportStack.isEmpty()) {
            Area child = this.getCurrentViewportArea();

            child.set(area);
            this.viewportStack.push(child);
        } else {
            Area current = this.viewportStack.peek();
            Area child = this.getCurrentViewportArea();

            child.set(area);
            current.clamp(child);
            this.viewportStack.push(child);
        }
    }

    private Area getCurrentViewportArea() {
        while (this.viewportAreas.size() < this.viewportStack.size() + 1) {
            this.viewportAreas.add(new Area());
        }

        return this.viewportAreas.get(this.viewportStack.size());
    }

    @Override
    public void popViewport() {
        this.viewportStack.pop();
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

        if (!this.viewportStack.isEmpty()) {
            this.viewportStack.peek().x += x;
        }
    }

    @Override
    public void shiftY(int y) {
        this.shiftY += y;

        if (!this.viewportStack.isEmpty()) {
            this.viewportStack.peek().y += y;
        }
    }
}