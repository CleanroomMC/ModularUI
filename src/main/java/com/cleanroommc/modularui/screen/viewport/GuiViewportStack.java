package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.api.layout.IViewportTransformation;
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

    private final Stack<Viewport> viewportStack = new Stack<>();
    private final List<Area> viewportAreas = new ArrayList<>();

    @Override
    public void reset() {
        this.viewportStack.clear();
    }

    @Override
    public Area getViewport() {
        return this.viewportStack.peek().area;
    }

    @Override
    public void pushViewport(IViewport viewport, Area area) {
        if (this.viewportStack.isEmpty()) {
            Area child = this.getCurrentViewportArea();

            child.set(area);
            this.viewportStack.push(new Viewport(viewport, area));
        } else {
            Area current = this.viewportStack.peek().area;
            Area child = this.getCurrentViewportArea();

            child.set(area);
            current.clamp(child);
            this.viewportStack.push(new Viewport(viewport, child));
        }
    }

    private Area getCurrentViewportArea() {
        while (this.viewportAreas.size() < this.viewportStack.size() + 1) {
            this.viewportAreas.add(new Area());
        }

        return this.viewportAreas.get(this.viewportStack.size());
    }

    @Override
    public void popViewport(IViewport viewport) {
        if (this.viewportStack.peek().viewport != viewport) {
            throw new IllegalStateException("Viewports must be popped in reverse order they were pushed. Tried to pop '" + viewport + "', but last pushed is '" + this.viewportStack.peek().viewport + "'.");
        }
        this.viewportStack.pop();
    }

    @Override
    public int getCurrentViewportIndex() {
        return this.viewportStack.size();
    }

    @Override
    public void popUntilIndex(int index) {
        for (int i = this.viewportStack.size() - 1; i > index; i--) {
            this.viewportStack.pop();
        }
    }

    @Override
    public void popUntilViewport(IViewport viewport) {
        int i = this.viewportStack.size();
        while (--i >= 0 && this.viewportStack.peek().viewport != viewport) {
            this.viewportStack.pop();
        }
    }

    @Override
    public void transform(IViewportTransformation transformation) {
        this.viewportStack.peek().transform(transformation);
    }

    @Override
    public void translate(int x, int y) {
        transform(new ViewportTranslation(x, y));
    }

    @Override
    public void scale(float x, float y) {
        transform(new ViewportScale(x, y));
    }

    @Override
    public int getShiftX() {
        return localX(0);
    }

    @Override
    public int getShiftY() {
        return localY(0);
    }

    /**
     * Get global X (relative to root element/screen)
     */
    @Override
    public int globalX(int x) {
        for (int i = this.viewportStack.size() - 1; i >= 0; i--) {
            x = this.viewportStack.get(i).transformX(x, false);
        }
        return x;
    }

    /**
     * Get global Y (relative to root element/screen)
     */
    @Override
    public int globalY(int y) {
        for (int i = this.viewportStack.size() - 1; i >= 0; i--) {
            y = this.viewportStack.get(i).transformY(y, false);
        }
        return y;
    }

    /**
     * Get current local X (relative to current viewport)
     */
    @Override
    public int localX(int x) {
        for (Viewport viewport : this.viewportStack) {
            x = viewport.transformX(x, true);
        }
        return x;
    }

    /**
     * Get current local Y (relative to current viewport)
     */
    @Override
    public int localY(int y) {
        for (Viewport viewport : this.viewportStack) {
            y = viewport.transformY(y, true);
        }
        return y;
    }

    @Override
    public void applyToOpenGl() {
        for (Viewport viewport : this.viewportStack) {
            viewport.transformation.applyOpenGlTransformation();
        }
    }

    @Override
    public void applyTopToOpenGl() {
        this.viewportStack.peek().transformation.applyOpenGlTransformation();
    }

    @Override
    public void unapplyToOpenGl() {
        for (int i = this.viewportStack.size() - 1; i >= 0; i--) {
            this.viewportStack.peek().transformation.unapplyOpenGlTransformation();
        }
    }

    @Override
    public void unapplyTopToOpenGl() {
        this.viewportStack.peek().transformation.unapplyOpenGlTransformation();
    }

    public static class Viewport {

        private final IViewport viewport;
        private final Area area;
        private IViewportTransformation transformation = IViewportTransformation.EMPTY;
        private TransformList transformations;

        public Viewport(IViewport viewport, Area area) {
            this.viewport = viewport;
            this.area = area;
        }

        public void transform(IViewportTransformation transformation) {
            if (this.transformation == IViewportTransformation.EMPTY) {
                this.transformation = transformation;
                return;
            }
            if (this.transformations == null) {
                this.transformations = new TransformList(this.transformation);
                this.transformation = this.transformations;
            }
            this.transformations.add(transformation);
        }

        public int transformX(int x, boolean toLocal) {
            return this.transformation.transformX(x, this.area, toLocal);
        }

        public int transformY(int y, boolean toLocal) {
            return this.transformation.transformY(y, this.area, toLocal);
        }
    }
}