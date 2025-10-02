package com.cleanroommc.modularui.api.layout;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.HoveredWidgetList;

import java.util.function.Predicate;

/**
 * A gui element which can transform its children f.e. a scrollable list.
 */
public interface IViewport extends IWidget {

    /**
     * Apply shifts of this viewport.
     *
     * @param stack viewport stack
     */
    default void transformChildren(IViewportStack stack) {}

    /**
     * Gathers all children at a position. Transformations from this viewport are already applied.
     *
     * @param stack   current viewport stack. Should not be modified.
     * @param widgets widget list of already gathered widgets. Add children here.
     * @param x       x position
     * @param y       y position
     */
    default void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (hasChildren()) {
            getChildrenAt(this, stack, widgets, x, y);
        }
    }

    /**
     * Gathers all children at a position. Transformations from this viewport are not applied.
     * Called before {@link #getWidgetsAt(IViewportStack, HoveredWidgetList, int, int)}
     *
     * @param stack   current viewport stack. Should not be modified.
     * @param widgets widget list of already gathered widgets. Add children here.
     * @param x       x position
     * @param y       y position
     */
    default void getSelfAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
        if (isInside(stack, x, y)) {
            widgets.add(this, stack, getAdditionalHoverInfo(stack, x, y));
        }
    }

    /**
     * Called during drawing twice (before children are drawn). Once with transformation of this viewport and once without
     *
     * @param context     gui context
     * @param transformed if transformation from this viewport is active
     */
    default void preDraw(ModularGuiContext context, boolean transformed) {}

    /**
     * Called during drawing twice (after children are drawn). Once with transformation of this viewport and once without
     *
     * @param context     gui context
     * @param transformed if transformation from this viewport is active
     */
    default void postDraw(ModularGuiContext context, boolean transformed) {}

    static void getChildrenAt(IWidget parent, IViewportStack stack, HoveredWidgetList widgetList, int x, int y) {
        for (IWidget child : parent.getChildren()) {
            if (!child.isEnabled()) {
                continue;
            }
            if (child instanceof IViewport viewport) {
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
                    widgetList.add(child, stack, child.getAdditionalHoverInfo(stack, x, y));
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
            if (child instanceof IViewport viewport) {
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
}
