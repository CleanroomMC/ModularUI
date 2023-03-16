package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.IResizeable;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

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

    public static void drawTree(IWidget parent, GuiContext context, float partialTicks) {
        drawTree(parent, context, false, partialTicks);
    }

    public static void drawTree(IWidget parent, GuiContext context, boolean ignoreEnabled, float partialTicks) {
        if (!parent.isEnabled() && !ignoreEnabled) return;

        GlStateManager.pushMatrix();
        Area screen = context.getViewport();
        int alpha = 1;//getWindow().getAlpha();
        float scale = 1;//getWindow().getScale();
        float sf = 1 / scale;
        // translate to center according to scale
        float x = parent.getArea().x;//(screen.x + screen.w / 2f * (1 - scale) + (parent.getArea().x - screen.x) * scale) * sf;
        float y = parent.getArea().y;//(screen.y + screen.h / 2f * (1 - scale) + (parent.getArea().y - screen.y) * scale) * sf;
        GlStateManager.translate(x, y, 0);
        GlStateManager.color(1, 1, 1, alpha);
        GlStateManager.enableBlend();
        context.applyToOpenGl();
        parent.drawBackground(context);
        parent.draw(context);

        IViewport viewport = parent instanceof IViewport ? (IViewport) parent : null;
        if (viewport != null) {
            viewport.preDraw(context, false);
            context.unapplyToOpenGl();
            viewport.apply(context);
            // update open gl transformation
            context.applyToOpenGl();
            viewport.preDraw(context, true);
        }
        GlStateManager.popMatrix();

        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            children.forEach(widget -> drawTree(widget, context, false, partialTicks));
        }
        if (viewport != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0);
            GlStateManager.color(1, 1, 1, alpha);
            GlStateManager.enableBlend();
            context.applyToOpenGl();
            viewport.postDraw(context, true);
            context.unapplyToOpenGl();
            viewport.unapply(context);
            // update open gl transformation
            context.applyToOpenGl();
            viewport.postDraw(context, false);
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
        WidgetTree.foreachChildByLayer(parent, child -> {
            IResizeable resizer = child.resizer();
            if (resizer != null) {
                resizer.applyPos(child);
            }
            return true;
        }, true);
        // make sure hovered widgets are updated
        parent.getPanel().markDirty();
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
}
