package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.api.IViewport;
import com.cleanroommc.modularui.api.IViewportStack;
import com.cleanroommc.modularui.api.IWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocatedWidget {

    public static LocatedWidget of(IWidget widget) {
        if (widget == null) {
            return EMPTY;
        }
        IWidget parent = widget;
        List<IViewport> viewports = new ArrayList<>();
        while (true) {
            if (parent instanceof IViewport) {
                viewports.add((IViewport) parent);
            }
            if (parent instanceof ModularPanel) {
                break;
            }
            parent = parent.getParent();
        }
        return new LocatedWidget(widget, viewports, false);
    }

    public static final LocatedWidget EMPTY = new LocatedWidget(null, Collections.emptyList(), false);

    private final IWidget widget;
    private final List<IViewport> viewports;

    public LocatedWidget(IWidget widget, List<IViewport> viewports) {
        this.widget = widget;
        this.viewports = new ArrayList<>(viewports);
    }

    private LocatedWidget(IWidget widget, List<IViewport> viewports, boolean copy) {
        this.widget = widget;
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

    public IWidget getWidget() {
        return widget;
    }
}
