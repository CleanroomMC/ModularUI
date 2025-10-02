package com.cleanroommc.modularui.screen.viewport;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;

import java.util.ArrayList;
import java.util.List;

public class LocatedWidget extends LocatedElement<IWidget> {

    private static final GuiViewportStack STACK = new GuiViewportStack();

    public static LocatedWidget of(IWidget widget) {
        if (widget == null) {
            return EMPTY;
        }
        // first make a list of all parents
        IWidget parent = widget;
        List<IWidget> ancestors = new ArrayList<>();
        while (true) {
            ancestors.add(0, parent);
            if (parent instanceof ModularPanel) {
                break;
            }
            parent = parent.getParent();
        }
        // iterate through each parent starting at the root and apply each transformation
        STACK.reset();
        for (IWidget widget1 : ancestors) {
            if (widget1 instanceof IViewport viewport) {
                STACK.pushViewport(viewport, widget1.getArea());
                widget1.transform(STACK);
                viewport.transformChildren(STACK);
            } else {
                STACK.pushMatrix();
                widget1.transform(STACK);
            }
        }
        return new LocatedWidget(widget, STACK.peek(), null);
    }

    public static final LocatedWidget EMPTY = new LocatedWidget(null, TransformationMatrix.EMPTY, null);

    private final Object additionalHoverInfo;

    public LocatedWidget(IWidget element, TransformationMatrix transformationMatrix, Object additionalHoverInfo) {
        super(element, transformationMatrix);
        this.additionalHoverInfo = additionalHoverInfo;
    }

    public Object getAdditionalHoverInfo() {
        return additionalHoverInfo;
    }

    @Override
    public String toString() {
        return "LocatedWidget[" + getElement() + " | " + additionalHoverInfo + "]";
    }
}
