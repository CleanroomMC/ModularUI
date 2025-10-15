package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.layout.ILayoutWidget;
import com.cleanroommc.modularui.api.layout.IResizeable;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.layout.IExpander;

import net.minecraft.client.renderer.GlStateManager;

import com.google.common.base.Joiner;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

@ApiStatus.Internal
class InternalWidgetTree {

    @Contract("_,_,_,_,false -> !null")
    @SuppressWarnings("unchecked")
    static <T extends IWidget> T findChildAt(IWidget parent, Class<T> type, String[] path, int index, boolean nullable) {
        String current = path[index];
        boolean isLast = index == path.length - 1;
        for (IWidget widget : parent.getChildren()) {
            if (widget.isName(current)) {
                if (isLast) {
                    if (!widget.isType(type)) {
                        throw new ClassCastException("Found widget at '" +
                                Joiner.on('/').join(Arrays.copyOfRange(path, 0, index + 1)) + "' with type '" +
                                widget.getClass().getName() + "', but expected type '" + type.getName() + "'.");
                    }
                    return (T) widget;
                }
                T result = findChildAt(widget, type, path, index + 1, nullable);
                if (result != null) return result;
            }
        }
        if (!nullable) {
            throw new NoSuchElementException("Expected to find widget at '" +
                    Joiner.on('/').join(Arrays.copyOfRange(path, 0, index + 1)) + "', but none was found.");
        }
        return null;
    }

    static void drawTree(IWidget parent, ModularGuiContext context, boolean ignoreEnabled, boolean drawBackground) {
        if (!parent.isEnabled() && !ignoreEnabled) return;
        if (parent.requiresResize()) {
            WidgetTree.resizeInternal(parent, false);
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

    static void drawBackground(IWidget parent, ModularGuiContext context, boolean ignoreEnabled) {
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

    static void drawTreeForeground(IWidget parent, ModularGuiContext context) {
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

    static boolean resizeWidget(IWidget widget, boolean init, boolean onOpen, boolean isParentLayout) {
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

    static void getTree(IWidget root, IWidget parent, Predicate<IWidget> test, StringBuilder builder, WidgetTree.WidgetInfo additionalInfo, String indent, boolean hasNextSibling) {
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
            List<IWidget> children = parent.getChildren();
            for (int i = 0; i < children.size(); i++) {
                IWidget child = children.get(i);
                if (test.test(child)) {
                    getTree(root, child, test, builder, additionalInfo, indent + (hasNextSibling ? "│ " : "  "), i < children.size() - 1);
                }
            }
        }
    }
}
