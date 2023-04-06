package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.IResizeable;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Helper class to apply actions to each widget in a tree.
 */
public class WidgetTree {

    private WidgetTree() {
    }

    public static List<IWidget> getAllChildrenByLayer(IWidget parent) {
        return getAllChildrenByLayer(parent, false);
    }

    public static List<IWidget> getAllChildrenByLayer(IWidget parent, boolean includeSelf) {
        List<IWidget> children = new ArrayList<>();
        if (includeSelf) children.add(parent);
        LinkedList<IWidget> parents = new LinkedList<>();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.pollFirst().getChildren()) {
                if (!child.getChildren().isEmpty()) {
                    parents.add(child);
                }
                children.add(child);
            }
        }
        return children;
    }

    public static boolean foreachChildByLayer(IWidget parent, Predicate<IWidget> consumer) {
        return foreachChildByLayer(parent, consumer, false);
    }

    public static boolean foreachChildByLayer(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        LinkedList<IWidget> parents = new LinkedList<>();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.pollFirst().getChildren()) {
                if (child.hasChildren()) {
                    parents.addLast(child);
                }
                if (!consumer.test(child)) return false;
            }
        }
        return true;
    }

    public static boolean foreachChildByLayer2(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        LinkedList<IWidget> parents = new LinkedList<>();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.pollFirst().getChildren()) {
                if (!consumer.test(child)) return false;

                if (child.hasChildren()) {
                    parents.addLast(child);
                }
            }
        }
        return true;
    }

    public static boolean foreachChild(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        if (parent.getChildren().isEmpty()) return true;
        for (IWidget widget : parent.getChildren()) {
            if (!consumer.test(widget)) return false;
            if (!widget.getChildren().isEmpty() && foreachChild(widget, consumer, false)) {
                return false;
            }
        }
        return true;
    }

    public static boolean foreachChildReverse(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (parent.getChildren().isEmpty()) {
            return !includeSelf || consumer.test(parent);
        }
        for (IWidget widget : parent.getChildren()) {
            if (!widget.getChildren().isEmpty() && foreachChildReverse(widget, consumer, false)) {
                return false;
            }
            if (!consumer.test(widget)) return false;
        }
        return !includeSelf || consumer.test(parent);
    }

    public static void drawTree(IWidget parent, GuiContext context) {
        drawTree(parent, context, false);
    }

    public static void drawTree(IWidget parent, GuiContext context, boolean ignoreEnabled) {
        if (!parent.isEnabled() && !ignoreEnabled) return;
        boolean canBeSeen = parent.canBeSeen();

        Area panel = parent.getPanel().getArea();
        IViewport viewport = parent instanceof IViewport ? (IViewport) parent : null;
        float alpha = 1f;
        float scale = 1f;
        float x = 0, y = 0;
        if (canBeSeen) {
            GlStateManager.pushMatrix();
            // get alpha and scale for open/close animation
            alpha = parent.getPanel().getAlpha();
            scale = parent.getPanel().getScale();
            float sf = 1 / scale;
            if (parent instanceof ModularPanel) {
                // panels just need to be translated to its center, scaled translated back to its top left corner
                GlStateManager.translate(parent.getArea().x, parent.getArea().y, 0);
                x = parent.getArea().width / 2f;
                y = parent.getArea().height / 2f;
                GlStateManager.translate(x, y, 0);
                GlStateManager.scale(scale, scale, 1);
                GlStateManager.translate(-x, -y, 0);
            } else {
                // all other needs to be scaled first
                GlStateManager.scale(scale, scale, 1);
                // then calculate translation with this complicated looking formula
                x = (panel.x + panel.w() / 2f * (1 - scale) + (parent.getArea().x - panel.x) * scale) * sf;
                y = (panel.y + panel.h() / 2f * (1 - scale) + (parent.getArea().y - panel.y) * scale) * sf;
                GlStateManager.translate(x, y, 0);
            }

            GlStateManager.color(1, 1, 1, alpha);
            GlStateManager.enableBlend();
            // now apply all active viewports to opengl
            context.applyToOpenGl();
            // draw the current widget
            parent.drawBackground(context);
            parent.draw(context);

            if (viewport != null) {
                // if this is a viewport we call some extra methods
                // first a normal draw call
                viewport.preDraw(context, false);
                // now push this viewport
                viewport.apply(context, IViewport.DRAWING | IViewport.PRE_DRAW);
                // apply only that viewport to opengl (since all others are already applied)
                context.applyTopToOpenGl();
                // draw again with the transformation
                viewport.preDraw(context, true);
            }
            // get rid of all opengl transformations for children
            // all widgets transform themselves on their own
            GlStateManager.popMatrix();
        } else if (viewport != null) {
            viewport.apply(context, IViewport.DRAWING | IViewport.PRE_DRAW);
        }

        // render all children if there are any
        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            children.forEach(widget -> drawTree(widget, context, false));
        }

        if (viewport != null) {
            if (!canBeSeen) {
                viewport.unapply(context, IViewport.DRAWING | IViewport.POST_DRAW);
                return;
            }
            // now apply the same transformations as above for open/close animation
            GlStateManager.pushMatrix();
            if (parent instanceof ModularPanel) {
                GlStateManager.translate(parent.getArea().x, parent.getArea().y, 0);
                GlStateManager.translate(x, y, 0);
                GlStateManager.scale(scale, scale, 1);
                GlStateManager.translate(-x, -y, 0);
            } else {
                GlStateManager.scale(scale, scale, 1);
                GlStateManager.translate(x, y, 0);
            }
            GlStateManager.color(1, 1, 1, alpha);
            GlStateManager.enableBlend();
            // now apply all active viewports to opengl again
            context.applyToOpenGl();
            // draw this viewport with active transformation
            viewport.postDraw(context, true);
            // only remove transformation of the top viewport
            context.unapplyTopToOpenGl();
            // pop the top viewport from the stack
            viewport.unapply(context, IViewport.DRAWING | IViewport.POST_DRAW);
            // draw again but without the top viewport transformation
            viewport.postDraw(context, false);
            // finally get rid of all opengl transformations
            GlStateManager.popMatrix();
        }
    }

    public static void drawTreeForeground(IWidget parent, GuiContext context) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        parent.drawForeground(context);

        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            children.forEach(widget -> drawTreeForeground(widget, context));
        }
    }

    @ApiStatus.Internal
    public static void onFrameUpdate(IWidget parent) {
        foreachChildByLayer(parent, widget -> {
            widget.onFrameUpdate();
            return true;
        }, true);
    }

    public static void resize(IWidget parent) {
        // resize each widget and calculate their relative pos
        parent.resize();
        // now apply the calculated pos
        applyPos(parent);
        WidgetTree.foreachChildByLayer(parent, child -> {
            child.postResize();
            return true;
        }, true);
    }

    public static void applyPos(IWidget parent) {
        WidgetTree.foreachChildByLayer(parent, child -> {
            IResizeable resizer = child.resizer();
            if (resizer != null) {
                resizer.applyPos(child);
            }
            return true;
        }, true);
    }

    public static IGuiElement findParent(IGuiElement parent, Predicate<IGuiElement> filter) {
        if (parent == null) return null;
        while (!(parent instanceof ModularPanel)) {
            if (filter.test(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return filter.test(parent) ? parent : null;
    }

    public static IWidget findParent(IWidget parent, Predicate<IWidget> filter) {
        if (parent == null) return null;
        while (!(parent instanceof ModularPanel)) {
            if (filter.test(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return filter.test(parent) ? parent : null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends IWidget> T findParent(IWidget parent, Class<T> type) {
        if (parent == null) return null;
        while (!(parent instanceof ModularPanel)) {
            if (type.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }
            parent = parent.getParent();
        }
        return type.isAssignableFrom(parent.getClass()) ? (T) parent : null;
    }
}
