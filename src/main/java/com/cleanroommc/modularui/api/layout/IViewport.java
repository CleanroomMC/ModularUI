package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.IWidgetList;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

import java.util.Stack;
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
     * @param stack   viewport stack
     * @param context the current context
     */
    void apply(IViewportStack stack, int context);

    /**
     * Undo shifts of this viewport.
     *
     * @param stack   viewport stack
     * @param context the current context
     */
    void unapply(IViewportStack stack, int context);

    /**
     * Gathers all children at a position. Transformations from this viewport are already applied.
     *
     * @param viewports current viewport stack. Should not be modified.
     * @param widgets   widget list of already gathered widgets. Add children here.
     * @param x         x position
     * @param y         y position
     */
    void getWidgetsAt(Stack<IViewport> viewports, IWidgetList widgets, int x, int y);

    /**
     * Gathers all children at a position. Transformations from this viewport are not applied.
     * Called before {@link #getWidgetsAt(Stack, IWidgetList, int, int)}
     *
     * @param viewports current viewport stack. Should not be modified.
     * @param widgets   widget list of already gathered widgets. Add children here.
     * @param x         x position
     * @param y         y position
     */
    default void getWidgetsBeforeApply(Stack<IViewport> viewports, IWidgetList widgets, int x, int y) {
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

    static void getChildrenAt(IWidget parent, Stack<IViewport> viewports, IWidgetList widgetList, int x, int y) {
        final int currentX = parent.getContext().localX(x);
        final int currentY = parent.getContext().localY(y);

        for (IWidget child : parent.getChildren()) {
            if (!child.isEnabled()) {
                continue;
            }
            if (child instanceof IViewport) {
                IViewport viewport = (IViewport) child;
                if (ModularUIConfig.debug) {
                    ModularUI.LOGGER.info("Gathering widgets in {}, at {}, {}", child, x, y);
                    ModularUI.LOGGER.info(" - viewports: {}", viewports);
                }
                int size = widgetList.size();
                viewport.getWidgetsBeforeApply(viewports, widgetList, x, y);
                viewports.push(viewport);
                viewport.apply(parent.getContext(), COLLECT_WIDGETS);
                viewport.getWidgetsAt(viewports, widgetList, x, y);
                viewport.unapply(parent.getContext(), COLLECT_WIDGETS);
                viewports.pop();
                if (ModularUIConfig.debug) {
                    ModularUI.LOGGER.info(" - found {} new children", widgetList.size() - size);
                }

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

    static boolean foreachChild(IWidget parent, Predicate<IWidget> predicate, int context) {
        for (IWidget child : parent.getChildren()) {
            if (!child.isEnabled()) {
                continue;
            }
            if (child instanceof IViewport) {
                IViewport viewport = (IViewport) child;
                viewport.apply(parent.getContext(), context);
                if (!predicate.test(child)) {
                    viewport.unapply(parent.getContext(), context);
                    return false;
                }
                if (child.hasChildren() && !foreachChild(child, predicate, context)) {
                    viewport.unapply(parent.getContext(), context);
                    return false;
                }
                viewport.unapply(parent.getContext(), context);
            } else {
                if (!predicate.test(child)) {
                    return false;
                }
                if (child.hasChildren() && !foreachChild(child, predicate, context)) {
                    return false;
                }
            }
        }
        return true;
    }
}