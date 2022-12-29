package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.IWidgetList;

import java.util.Stack;
import java.util.function.Predicate;

/**
 * An area in a GUI that can be shifted around. Used for scrollable widgets.
 */
public interface IViewport {

    /**
     * Apply shifts of this viewport.
     *
     * @param stack viewport stack
     */
    void apply(IViewportStack stack);

    /**
     * Undo shifts of this viewport.
     *
     * @param stack viewport stack
     */
    void unapply(IViewportStack stack);

    void getWidgetsAt(Stack<IViewport> viewports, IWidgetList widgets, int x, int y);

    static void getChildrenAt(IWidget parent, Stack<IViewport> viewports, IWidgetList widgetList, int x, int y) {
        final int currentX = parent.getContext().localX(x);
        final int currentY = parent.getContext().localY(y);

        for (IWidget child : parent.getChildren()) {
            if (!child.isEnabled()) {
                continue;
            }
            if (child instanceof IViewport) {
                IViewport viewport = (IViewport) child;
                viewports.push(viewport);
                viewport.apply(parent.getContext());
                viewport.getWidgetsAt(viewports, widgetList, x, y);
                viewport.unapply(parent.getContext());
                viewports.pop();
            } else {
                if (child.getArea().isInside(currentX, currentY)) {
                    widgetList.add(child, viewports);
                }
                if (child.hasChildren()) {
                    getChildrenAt(child, viewports, widgetList, x, y);
                }
            }
        }
    }

    static boolean foreachChild(IWidget parent, Predicate<IWidget> predicate) {
        for (IWidget child : parent.getChildren()) {
            if (!child.isEnabled()) {
                continue;
            }
            if (child instanceof IViewport) {
                IViewport viewport = (IViewport) child;
                viewport.apply(parent.getContext());
                if (!predicate.test(child)) {
                    viewport.unapply(parent.getContext());
                    return false;
                }
                if (child.hasChildren() && !foreachChild(child, predicate)) {
                    viewport.unapply(parent.getContext());
                    return false;
                }
                viewport.unapply(parent.getContext());
            } else {
                if (!predicate.test(child)) {
                    return false;
                }
                if (child.hasChildren() && !foreachChild(child, predicate)) {
                    return false;
                }
            }
        }
        return true;
    }
}