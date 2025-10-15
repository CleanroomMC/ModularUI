package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.layout.IResizeable;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.ISynced;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.utils.ObjectList;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.layout.IExpander;

import net.minecraft.client.renderer.GlStateManager;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Helper class to apply actions to each widget in a tree.
 */
public class WidgetTree {

    public static boolean logResizeTime = false;

    private WidgetTree() {}

    public static List<IWidget> getAllChildrenByLayer(IWidget parent) {
        return getAllChildrenByLayer(parent, false);
    }

    public static List<IWidget> getAllChildrenByLayer(IWidget parent, boolean includeSelf) {
        List<IWidget> children = new ArrayList<>();
        if (includeSelf) children.add(parent);
        ObjectList<IWidget> parents = ObjectList.create();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.removeFirst().getChildren()) {
                if (!child.getChildren().isEmpty()) {
                    parents.add(child);
                }
                children.add(child);
            }
        }
        return children;
    }

    public static boolean foreachChildBFS(IWidget parent, Predicate<IWidget> consumer) {
        return foreachChildBFS(parent, consumer, false);
    }

    public static boolean foreachChildBFS(IWidget parent, Predicate<IWidget> consumer, boolean includeSelf) {
        if (includeSelf && !consumer.test(parent)) return false;
        ObjectList<IWidget> parents = ObjectList.create();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.removeFirst().getChildren()) {
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
        ObjectList<IWidget> parents = ObjectList.create();
        parents.add(parent);
        while (!parents.isEmpty()) {
            for (IWidget child : parents.removeFirst().getChildren()) {
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

    public static void drawTree(IWidget parent, ModularGuiContext context) {
        drawTree(parent, context, false, true);
    }

    public static void drawTree(IWidget parent, ModularGuiContext context, boolean ignoreEnabled, boolean drawBackground) {
        if (!parent.isEnabled() && !ignoreEnabled) return;
        if (parent.requiresResize()) {
            resizeInternal(parent, false);
        }

        float alpha = parent.getPanel().getAlpha();
        IViewport viewport = parent instanceof IViewport ? (IViewport) parent : null;

        // transform stack according to the widget
        context.pushMatrix();
        parent.transform(context);

        boolean canBeSeen = parent.canBeSeen(context);

        // apply transformations to opengl
        GlStateManager.pushMatrix();
        context.applyToOpenGl();

        GlStateManager.colorMask(true, true, true, true);
        if (canBeSeen) {
            // draw widget
            GlStateManager.color(1f, 1f, 1f, alpha);
            WidgetThemeEntry<?> widgetTheme = parent.getWidgetTheme(context.getTheme());
            if (drawBackground) parent.drawBackground(context, widgetTheme);
            parent.draw(context, widgetTheme);
            parent.drawOverlay(context, widgetTheme);
        }

        if (viewport != null) {
            if (canBeSeen) {
                // draw viewport without children transformation
                GlStateManager.color(1f, 1f, 1f, alpha);
                viewport.preDraw(context, false);
                GlStateManager.popMatrix();
                // apply children transformation of the viewport
                context.pushViewport(viewport, parent.getArea());
                viewport.transformChildren(context);
                // apply to opengl and draw with transformation
                GlStateManager.pushMatrix();
                context.applyToOpenGl();
                viewport.preDraw(context, true);
            } else {
                // only transform stack
                context.pushViewport(viewport, parent.getArea());
                viewport.transformChildren(context);
            }
        }
        // remove all opengl transformations
        GlStateManager.popMatrix();

        // render all children if there are any
        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            boolean backgroundSeparate = children.size() > 1;
            // draw all backgrounds first if we have more than 1 child
            // the whole reason this exists is because of the hover animation of items with NEA
            // on hover the item scales up slightly, this causes the amount text to overlap nearby slots, but since the whole slot is drawn
            // at once the backgrounds might draw on top of the text
            // for now we'll apply this always without checking for NEA as it might be useful for other things
            // maybe proper layer customization in the future?
            if (backgroundSeparate) children.forEach(widget -> drawBackground(widget, context, ignoreEnabled));
            children.forEach(widget -> drawTree(widget, context, false, !backgroundSeparate));
        }

        if (viewport != null) {
            if (canBeSeen) {
                // apply opengl transformations again and draw
                GlStateManager.color(1f, 1f, 1f, alpha);
                GlStateManager.pushMatrix();
                context.applyToOpenGl();
                viewport.postDraw(context, true);
                // remove children transformation of this viewport
                context.popViewport(viewport);
                GlStateManager.popMatrix();
                // apply transformation again to opengl and draw
                GlStateManager.pushMatrix();
                context.applyToOpenGl();
                viewport.postDraw(context, false);
                GlStateManager.popMatrix();
            } else {
                // only remove transformation
                context.popViewport(viewport);
            }
        }
        // remove all widget transformations
        context.popMatrix();
    }

    public static void drawBackground(IWidget parent, ModularGuiContext context, boolean ignoreEnabled) {
        if (!parent.isEnabled() && !ignoreEnabled) return;

        float alpha = parent.getPanel().getAlpha();

        // transform stack according to the widget
        context.pushMatrix();
        parent.transform(context);

        boolean canBeSeen = parent.canBeSeen(context);
        if (!canBeSeen) {
            context.popMatrix();
            return;
        }

        // apply transformations to opengl
        GlStateManager.pushMatrix();
        context.applyToOpenGl();

        // draw widget
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.color(1f, 1f, 1f, alpha);
        WidgetThemeEntry<?> widgetTheme = parent.getWidgetTheme(context.getTheme());
        parent.drawBackground(context, widgetTheme);

        GlStateManager.popMatrix();
        context.popMatrix();
    }

    public static void drawTreeForeground(IWidget parent, ModularGuiContext context) {
        IViewport viewport = parent instanceof IViewport viewport1 ? viewport1 : null;
        context.pushMatrix();
        parent.transform(context);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        parent.drawForeground(context);

        List<IWidget> children = parent.getChildren();
        if (!children.isEmpty()) {
            if (viewport != null) {
                context.pushViewport(viewport, parent.getArea());
                viewport.transformChildren(context);
            }
            children.forEach(widget -> drawTreeForeground(widget, context));
            if (viewport != null) context.popViewport(viewport);
        }
        context.popMatrix();
    }

    @ApiStatus.Internal
    public static void onUpdate(IWidget parent) {
        foreachChildBFS(parent, widget -> {
            widget.onUpdate();
            return true;
        }, true);
    }

    @Deprecated
    public static void resize(IWidget parent) {
        parent.scheduleResize();
    }

    @ApiStatus.Internal
    public static void resizeInternal(IWidget parent, boolean onOpen) {
        long fullTime = System.nanoTime();
        // check if updating this widget's pos and size can potentially update its parents
        while (!(parent instanceof ModularPanel) && (parent.getParent() instanceof ILayoutWidget || parent.getParent().flex().dependsOnChildren())) {
            parent = parent.getParent();
        }
        long rawTime = System.nanoTime();
        // resize each widget and calculate their relative pos
        if (!resizeWidget(parent, true, onOpen, false) && !resizeWidget(parent, false, onOpen, false)) {
            if (WidgetTree.logResizeTime) {
                rawTime = System.nanoTime() - rawTime;
                ModularUI.LOGGER.error("Failed to resize widget tree in {} µs.", NumberFormat.formatNanosToMicros(rawTime));
            }
            throw new IllegalStateException("Failed to resize widgets");
        }
        rawTime = System.nanoTime() - rawTime;
        // now apply the calculated pos
        applyPos(parent);
        WidgetTree.foreachChildBFS(parent, child -> {
            child.postResize();
            return true;
        }, true);

        if (WidgetTree.logResizeTime) {
            fullTime = System.nanoTime() - fullTime;
            ModularUI.LOGGER.info("Resized widget tree in {} µs and {} µs for full resize.",
                    NumberFormat.formatNanosToMicros(rawTime),
                    NumberFormat.formatNanosToMicros(fullTime));
        }
    }

    private static boolean resizeWidget(IWidget widget, boolean init, boolean onOpen, boolean isParentLayout) {
        boolean alreadyCalculated = false;
        // first try to resize this widget
        IResizeable resizer = widget.resizer();
        ILayoutWidget layout = widget instanceof ILayoutWidget layoutWidget ? layoutWidget : null;
        boolean isLayout = layout != null;
        if (init) {
            widget.beforeResize(onOpen);
            resizer.initResizing();
            if (!isLayout) resizer.setLayoutDone(true);
        } else {
            // if this is not the first time check if this widget is already resized
            alreadyCalculated = resizer.isFullyCalculated(isParentLayout);
        }
        boolean selfFullyCalculated = resizer.isSelfFullyCalculated() || resizer.resize(widget, isParentLayout);

        GuiAxis expandAxis = widget instanceof IExpander expander ? expander.getExpandAxis() : null;
        // now resize all children and collect children which could not be fully calculated
        List<IWidget> anotherResize = Collections.emptyList();
        if (!resizer.areChildrenCalculated() && widget.hasChildren()) {
            anotherResize = new ArrayList<>();
            for (IWidget child : widget.getChildren()) {
                if (init) child.flex().checkExpanded(expandAxis);
                if (!resizeWidget(child, init, onOpen, isLayout)) {
                    anotherResize.add(child);
                }
            }
        }

        if (init || !resizer.areChildrenCalculated() || !resizer.isLayoutDone()) {
            boolean layoutSuccessful = true;
            // we need to keep track of which widgets are not yet fully calculated, so we can call onResized ont those which later are
            // fully calculated
            BitSet state = getCalculatedState(anotherResize, isLayout);
            if (layout != null) {
                layoutSuccessful = layout.layoutWidgets();
            }

            // post resize this widget if possible
            if (!selfFullyCalculated) {
                resizer.postResize(widget);
            }

            if (layout != null) {
                layoutSuccessful &= layout.postLayoutWidgets();
            }
            resizer.setLayoutDone(layoutSuccessful);
            checkFullyCalculated(anotherResize, state, isLayout);
        }

        // now fully resize all children which needs it
        if (!anotherResize.isEmpty()) {
            for (int i = 0; i < anotherResize.size(); i++) {
                if (resizeWidget(anotherResize.get(i), false, onOpen, isLayout)) {
                    anotherResize.remove(i--);
                }
            }
        }
        resizer.setChildrenResized(anotherResize.isEmpty());
        selfFullyCalculated = resizer.isFullyCalculated(isParentLayout);

        if (selfFullyCalculated && !alreadyCalculated) widget.onResized();

        return selfFullyCalculated;
    }

    private static BitSet getCalculatedState(List<IWidget> children, boolean isLayout) {
        if (children.isEmpty()) return null;
        BitSet state = new BitSet();
        for (int i = 0; i < children.size(); i++) {
            IWidget widget = children.get(i);
            if (widget.resizer().isFullyCalculated(isLayout)) {
                state.set(i);
            }
        }
        return state;
    }

    private static void checkFullyCalculated(List<IWidget> children, BitSet state, boolean isLayout) {
        if (children.isEmpty() || state == null) return;
        int j = 0;
        for (int i = 0; i < children.size(); i++) {
            IWidget widget = children.get(i);
            if (!state.get(j) && widget.resizer().isFullyCalculated(isLayout)) {
                widget.onResized();
                state.set(j);
                children.remove(i--);
            }
            j++;
        }
    }

    public static void applyPos(IWidget parent) {
        WidgetTree.foreachChildBFS(parent, child -> {
            child.resizer().applyPos(child);
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

    @ApiStatus.Internal
    public static void collectSyncValues(PanelSyncManager syncManager, ModularPanel panel) {
        collectSyncValues(syncManager, panel, true);
    }

    @ApiStatus.Internal
    public static void collectSyncValues(PanelSyncManager syncManager, ModularPanel panel, boolean includePanel) {
        collectSyncValues(syncManager, panel.getName(), panel, includePanel);
    }

    @ApiStatus.Internal
    public static void collectSyncValues(PanelSyncManager syncManager, String panelName, IWidget panel, boolean includePanel) {
        AtomicInteger id = new AtomicInteger(0);
        String syncKey = ModularSyncManager.AUTO_SYNC_PREFIX + panelName;
        foreachChildBFS(panel, widget -> {
            if (widget instanceof ISynced<?> synced) {
                if (synced.isSynced() && !syncManager.hasSyncHandler(synced.getSyncHandler())) {
                    syncManager.syncValue(syncKey, id.getAndIncrement(), synced.getSyncHandler());
                }
            }
            return true;
        }, includePanel);
    }

    public static boolean hasSyncedValues(ModularPanel panel) {
        return !foreachChildBFS(panel, widget -> !(widget instanceof ISynced<?> synced) || !synced.isSynced(), true);
    }

    public static void print(IWidget parent, Predicate<IWidget> test) {
        StringBuilder builder = new StringBuilder("Widget tree of ")
                .append(parent)
                .append('\n');
        getTree(parent.getArea(), parent, test, builder, 0);
        ModularUI.LOGGER.info(builder.toString());
    }

    private static void getTree(Area root, IWidget parent, Predicate<IWidget> test, StringBuilder builder, int indent) {
        if (indent >= 2) {
            builder.append(StringUtils.repeat(' ', indent - 2))
                    .append("- ");
        }
        builder.append(parent).append(" {")
                .append(parent.getArea().x - root.x)
                .append(", ")
                .append(parent.getArea().y - root.y)
                .append(" | ")
                .append(parent.getArea().width)
                .append(", ")
                .append(parent.getArea().height)
                .append("}\n");
        if (parent.hasChildren()) {
            for (IWidget child : parent.getChildren()) {
                if (test.test(child)) {
                    getTree(root, child, test, builder, indent + 2);
                }
            }
        }
    }
}
