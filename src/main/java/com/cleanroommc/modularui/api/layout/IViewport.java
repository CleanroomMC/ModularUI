package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.IWidgetList;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

import java.util.function.Predicate;

/**
 * An area in a GUI that can be shifted around. Used for scrollable widgets.
 */
public interface IViewport {

    int DRAWING = 1;
    int INTERACTION = 1 << 1;
    int PRE_DRAW = 1 << 2;
    int POST_DRAW = 1 << 3;
    int START_DRAGGING = 1 << 4;
    int STOP_DRAGGING = 1 << 5;
    int MOUSE = 1 << 6;
    int KEY = 1 << 7;
    int PRESSED = 1 << 8;
    int RELEASED = 1 << 9;
    int SCROLL = 1 << 10;
    int COLLECT_WIDGETS = 1 << 11;
    int DRAGGABLE = 1 << 12;
    int DRAG = 1 << 13;
    int DEBUG = 1 << 14;

    /**
     * Apply shifts of this viewport.
     *
     * @param stack viewport stack
     */
    default void transformChildren(IViewportStack stack) {
    }

    /**
     * Gathers all children at a position. Transformations from this viewport are already applied.
     *
     * @param stack   current viewport stack. Should not be modified.
     * @param widgets widget list of already gathered widgets. Add children here.
     * @param x       x position
     * @param y       y position
     */
    void getWidgetsAt(IViewportStack stack, IWidgetList widgets, int x, int y);

    /**
     * Gathers all children at a position. Transformations from this viewport are not applied.
     * Called before {@link #getWidgetsAt(IViewportStack, IWidgetList, int, int)}
     *
     * @param stack   current viewport stack. Should not be modified.
     * @param widgets widget list of already gathered widgets. Add children here.
     * @param x       x position
     * @param y       y position
     */
    default void getSelfAt(IViewportStack stack, IWidgetList widgets, int x, int y) {
    }

    /**
     * Called during drawing twice (before children are drawn). Once with transformation of this viewport and once without
     *
     * @param context     gui context
     * @param transformed if transformation from this viewport is active
     */
    default void preDraw(GuiContext context, boolean transformed) {
    }

    /**
     * Called during drawing twice (after children are drawn). Once with transformation of this viewport and once without
     *
     * @param context     gui context
     * @param transformed if transformation from this viewport is active
     */
    default void postDraw(GuiContext context, boolean transformed) {
    }

    static void getChildrenAt(IWidget parent, IViewportStack stack, IWidgetList widgetList, int x, int y) {
        for (IWidget child : parent.getChildren()) {
            if (!child.isEnabled()) {
                continue;
            }
            if (child instanceof IViewport) {
                IViewport viewport = (IViewport) child;
                stack.pushViewport(viewport, parent.getArea());
                child.transform(stack);
                viewport.getSelfAt(stack, widgetList, x, y);
                viewport.transformChildren(stack);
                viewport.getWidgetsAt(stack, widgetList, x, y);
                stack.popViewport(viewport);
            } else {
                stack.pushMatrix();
                child.transform(stack);
                if (child.isInside(stack, x, y)) {
                    widgetList.add(child, stack.peek());
                }
                if (child.hasChildren()) {
                    getChildrenAt(child, stack, widgetList, x, y);
                }
                stack.popMatrix();
            }
        }
    }

    static boolean foreachChild(IViewportStack stack, IWidget parent, Predicate<IWidget> predicate, int context) {
        for (IWidget child : parent.getChildren()) {
            if (!child.isEnabled()) {
                continue;
            }
            stack.popMatrix();
            if (child instanceof IViewport) {
                IViewport viewport = (IViewport) child;
                stack.pushViewport(viewport, parent.getArea());
                parent.transform(stack);
                if (!predicate.test(child)) {
                    stack.popViewport(viewport);
                    return false;
                }
                viewport.transformChildren(parent.getContext());
                if (child.hasChildren() && !foreachChild(stack, child, predicate, context)) {
                    stack.popViewport(viewport);
                    return false;
                }
                stack.popViewport(viewport);
            } else {
                stack.pushMatrix();
                parent.transform(stack);
                if (!predicate.test(child)) {
                    stack.popMatrix();
                    return false;
                }
                if (child.hasChildren() && !foreachChild(stack, child, predicate, context)) {
                    stack.popMatrix();
                    return false;
                }
                stack.popMatrix();
            }
        }
        return true;
    }

    IViewport EMPTY = new IViewport() {

        @Override
        public void transformChildren(IViewportStack stack) {
        }

        @Override
        public void getWidgetsAt(IViewportStack viewports, IWidgetList widgets, int x, int y) {
        }
    };
}