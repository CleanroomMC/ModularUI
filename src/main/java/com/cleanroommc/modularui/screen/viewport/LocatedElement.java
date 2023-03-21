package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;

import java.util.ArrayList;
import java.util.List;

public class LocatedElement<T> {

    private final T element;
    private final List<IViewport> viewports;

    public LocatedElement(T element, List<IViewport> viewports) {
        this.element = element;
        this.viewports = new ArrayList<>(viewports);
    }

    LocatedElement(T element, List<IViewport> viewports, boolean copy) {
        this.element = element;
        this.viewports = viewports;
    }

    public void applyViewports(IViewportStack viewportStack, int context) {
        applyViewports(viewportStack, context, 0);
    }

    public void applyViewports(IViewportStack viewportStack, int context, int start) {
        for (int i = start, n = this.viewports.size(); i < n; i++) {
            this.viewports.get(i).apply(viewportStack, context);
        }
    }

    public void unapplyViewports(IViewportStack viewportStack, int context) {
        unapplyViewports(viewportStack, context, 0);
    }

    public void unapplyViewports(IViewportStack viewportStack, int context, int until) {
        for (int i = this.viewports.size() - 1; i >= until; i--) {
            this.viewports.get(i).unapply(viewportStack, context);
        }
    }

    public T getElement() {
        return element;
    }

    public List<IViewport> getViewports() {
        return viewports;
    }
}
