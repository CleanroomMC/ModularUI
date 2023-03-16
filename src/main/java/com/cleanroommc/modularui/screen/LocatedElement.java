package com.cleanroommc.modularui.screen;

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

    public void applyViewports(IViewportStack viewportStack) {
        for (IViewport viewport : viewports) {
            viewport.apply(viewportStack);
        }
    }

    public void unapplyViewports(IViewportStack viewportStack) {
        for (IViewport viewport : viewports) {
            viewport.unapply(viewportStack);
        }
    }

    public T getElement() {
        return element;
    }

    public List<IViewport> getViewports() {
        return viewports;
    }
}
