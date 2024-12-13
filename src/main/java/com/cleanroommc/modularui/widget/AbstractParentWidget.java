package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widgets.VoidWidget;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A widget which can hold any amount of children.
 *
 * @param <I> type of children (in most cases just {@link IWidget}). Use {@link VoidWidget} if no children should be added.
 * @param <W> type of this widget
 */
public class AbstractParentWidget<I extends IWidget, W extends AbstractParentWidget<I, W>> extends Widget<W> {

    private final List<I> children = new ArrayList<>();

    @NotNull
    @Override
    public List<IWidget> getChildren() {
        return (List<IWidget>) this.children;
    }

    public List<I> getTypeChildren() {
        return children;
    }

    @Override
    public boolean canHover() {
        if (IDrawable.isVisible(getBackground()) ||
                IDrawable.isVisible(getHoverBackground()) ||
                IDrawable.isVisible(getHoverOverlay()) ||
                getTooltip() != null) return true;
        WidgetTheme widgetTheme = getWidgetTheme(getContext().getTheme());
        if (getBackground() == null && IDrawable.isVisible(widgetTheme.getBackground())) return true;
        return getHoverBackground() == null && IDrawable.isVisible(widgetTheme.getHoverBackground());
    }

    @Override
    public boolean canClickThrough() {
        return !canHover();
    }

    protected boolean addChild(I child, int index) {
        if (child == null || child == this || getChildren().contains(child)) {
            return false;
        }
        if (child instanceof ModularPanel) {
            throw new IllegalArgumentException("ModularPanel should not be added as child widget; Use ModularScreen#openPanel instead");
        }
        if (!isChildValid(child)) {
            throw new IllegalArgumentException("Child '" + child + "' is not valid for parent '" + this + "'!");
        }
        if (index < 0) {
            index += getChildren().size() + 1;
        }
        this.children.add(index, child);
        if (isValid()) {
            child.initialise(this);
        }
        onChildAdd(child);
        return true;
    }

    protected boolean remove(I child) {
        if (this.children.remove(child)) {
            child.dispose();
            onChildRemove(child);
            return true;
        }
        return false;
    }

    protected boolean remove(int index) {
        if (index < 0) {
            index = getChildren().size() + index + 1;
        }
        I child = this.children.remove(index);
        child.dispose();
        onChildRemove(child);
        return true;
    }

    protected boolean isChildValid(I child) {
        return true;
    }

    protected void onChildAdd(I child) {}

    protected void onChildRemove(I child) {}
}
