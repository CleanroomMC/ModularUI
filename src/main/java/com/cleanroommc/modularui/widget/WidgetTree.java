package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.drawable.IKey;
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
import com.cleanroommc.modularui.widgets.layout.IExpander;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentString;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

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

    public static boolean logResizeTime = true;

    public static final WidgetInfo INFO_AREA = (root, widget, builder) -> builder
            .append("Area xywh:")
            .append(widget.getArea().x - root.getArea().x)
            .append(", ")
            .append(widget.getArea().y - root.getArea().y)
            .append(", ")
            .append(widget.getArea().width)
            .append(", ")
            .append(widget.getArea().height);
    public static final WidgetInfo INFO_ENABLED = (root, widget, builder) -> builder.append("Enabled: ").append(widget.isEnabled());
    public static final WidgetInfo INFO_FULLY_RESIZED = (root, widget, builder) -> builder
            .append("Fully resized: ")
            .append(widget.resizer().isFullyCalculated(widget.hasParent() && widget.getParent() instanceof ILayoutWidget));
    public static final WidgetInfo INFO_RESIZED_DETAILED = (root, widget, builder) -> builder
            .append("Self resized: ").append(widget.resizer().isSelfFullyCalculated(widget.hasParent() && widget.getParent() instanceof ILayoutWidget))
            .append(", Is pos final: ").append(!widget.resizer().canRelayout(widget.hasParent() && widget.getParent() instanceof ILayoutWidget))
            .append(", Children resized: ").append(widget.resizer().areChildrenCalculated())
            .append(", Layout done: ").append(widget.resizer().isLayoutDone());
    public static final WidgetInfo INFO_RESIZED_COLLAPSED = (root, widget, builder) -> {
        if (widget.resizer().isFullyCalculated(widget.hasParent() && widget.getParent() instanceof ILayoutWidget)) {
            INFO_FULLY_RESIZED.addInfo(root, widget, builder);
        } else {
            INFO_RESIZED_DETAILED.addInfo(root, widget, builder);
        }
    };
    public static final WidgetInfo INFO_WIDGET_THEME = (root, widget, builder) -> builder.append("Widget theme: ")
            .append(widget.getWidgetTheme(widget.getContext().getTheme()).getKey().getFullName());

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
            if (MCHelper.getPlayer() != null) {
                MCHelper.getPlayer().sendMessage(new TextComponentString(IKey.RED + "ModularUI: Failed to resize sub tree of widget '"
                        + parent + "' of screen '" + parent.getScreen().toString() + "'. See log for more info."));
            }
            ModularUI.LOGGER.error("Failed to resize widget. Affected widget tree:");
            printTree(parent, INFO_RESIZED_COLLAPSED);
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
            ModularUI.LOGGER.info("Resized widget tree in {}s and {}s for full resize.",
                    NumberFormat.formatNanos(rawTime),
                    NumberFormat.formatNanos(fullTime));
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

    public static void printTree(IWidget parent) {
        printTree(parent, w -> true, null);
    }

    public static void printTree(IWidget parent, WidgetInfo additionalInfo) {
        printTree(parent, w -> true, additionalInfo);
    }

    public static void printTree(IWidget parent, Predicate<IWidget> test) {
        printTree(parent, test, null);
    }

    public static void printTree(IWidget parent, Predicate<IWidget> test, WidgetInfo additionalInfo) {
        StringBuilder builder = new StringBuilder("Widget tree of ")
                .append(parent)
                .append('\n');
        ModularUI.LOGGER.info(widgetTreeToString(builder, parent, test, additionalInfo));
    }

    public static StringBuilder widgetTreeToString(StringBuilder builder, IWidget parent, Predicate<IWidget> test, WidgetInfo additionalInfo) {
        getTree(parent, parent, test, builder, additionalInfo, "", false);
        return builder;
    }

    private static void getTree(IWidget root, IWidget parent, Predicate<IWidget> test, StringBuilder builder, WidgetInfo additionalInfo, String indent, boolean hasNextSibling) {
        if (!indent.isEmpty()) {
            builder.append(indent).append(hasNextSibling ? "├ " : "└ ");
        }
        builder.append(parent);
        if (additionalInfo != null) {
            builder.append(" {");
            additionalInfo.addInfo(root, parent, builder);
            builder.append("}");
        }
        builder.append('\n');
        if (parent.hasChildren()) {
            @NotNull List<IWidget> children = parent.getChildren();
            for (int i = 0; i < children.size(); i++) {
                IWidget child = children.get(i);
                if (test.test(child)) {
                    getTree(root, child, test, builder, additionalInfo, indent + (hasNextSibling ? "│ " : "  "), i < children.size() - 1);
                }
            }
        }
    }

    public interface WidgetInfo {

        void addInfo(IWidget root, IWidget widget, StringBuilder builder);

        default WidgetInfo combine(WidgetInfo other, String joiner) {
            return (root, widget, builder) -> {
                addInfo(root, widget, builder);
                builder.append(joiner);
                other.addInfo(root, widget, builder);
            };
        }

        default WidgetInfo combine(WidgetInfo other) {
            return combine(other, " ");
        }

        static WidgetInfo of(String joiner, WidgetInfo... infos) {
            return (root, widget, builder) -> {
                for (int i = 0; i < infos.length; i++) {
                    WidgetInfo info = infos[i];
                    info.addInfo(root, widget, builder);
                    if (i < infos.length - 1) {
                        builder.append(joiner);
                    }
                }
            };
        }

        static WidgetInfo of(WidgetInfo... infos) {
            return of(" ", infos);
        }
    }
}
