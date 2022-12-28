package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.IViewport;
import com.cleanroommc.modularui.api.IWidget;
import com.cleanroommc.modularui.screen.GuiContext;
import com.cleanroommc.modularui.screen.LocatedWidget;
import com.cleanroommc.modularui.widget.sizer.Area;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.GlStateManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
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

    @ApiStatus.Internal
    public static void drawInternal(IWidget parent, GuiContext context, float partialTicks) {
        if (!parent.isEnabled()) return;
        if (parent instanceof IViewport) {
            ((IViewport) parent).apply(context);
        }
        GlStateManager.pushMatrix();
        Area viewport = context.getViewport();
        int alpha = 1;//getWindow().getAlpha();
        float scale = 1;//getWindow().getScale();
        float sf = 1 / scale;
        // translate to center according to scale
        float x = parent.getArea().x;//(viewport.x + viewport.w / 2f * (1 - scale) + (parent.getArea().x - viewport.x) * scale) * sf;
        float y = parent.getArea().y;//(viewport.y + viewport.h / 2f * (1 - scale) + (parent.getArea().y - viewport.y) * scale) * sf;
        GlStateManager.translate(x, y, 0);
        GlStateManager.color(1, 1, 1, alpha);
        GlStateManager.enableBlend();
        parent.drawBackground(partialTicks);
        parent.draw(partialTicks);
        GlStateManager.popMatrix();

        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            children.forEach(widget -> drawInternal(widget, context, partialTicks));
        }
        if (parent instanceof IViewport) {
            ((IViewport) parent).unapply(context);
        }
    }

    @ApiStatus.Internal
    public static void drawForegroundInternal(IWidget parent, float partialTicks) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        parent.drawForeground(partialTicks);

        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            children.forEach(widget -> drawForegroundInternal(widget, partialTicks));
        }
    }

    @ApiStatus.Internal
    public static void onFrameUpdate(IWidget parent) {
        foreachChildByLayer(parent, widget -> {
            widget.onFrameUpdate();
            return true;
        }, true);
    }
}
