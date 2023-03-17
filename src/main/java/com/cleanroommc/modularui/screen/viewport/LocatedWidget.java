package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocatedWidget extends LocatedElement<IWidget> {

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

    public LocatedWidget(IWidget element, List<IViewport> viewports) {
        super(element, viewports);
    }

    LocatedWidget(IWidget element, List<IViewport> viewports, boolean copy) {
        super(element, viewports, copy);
    }
}
