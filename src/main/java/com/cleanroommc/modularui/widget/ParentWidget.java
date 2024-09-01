package com.cleanroommc.modularui.widget;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.theme.WidgetTheme;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ParentWidget<W extends ParentWidget<W>> extends Widget<W> {

    private final List<IWidget> children = new ArrayList<>();

    @NotNull
    @Override
    public List<IWidget> getChildren() {
        return this.children;
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

    public boolean addChild(IWidget child, int index) {
        if (child == null || child == this || getChildren().contains(child)) {
            return false;
        }
        if (child instanceof ModularPanel) {
            throw new IllegalStateException("ModularPanel should not be added as child widget; Use ModularScreen#openPanel instead");
        }
        if (index < 0) {
            index = getChildren().size() + index + 1;
        }
        this.children.add(index, child);
        if (isValid()) {
            child.initialise(this);
        }
        onChildAdd(child);
        return true;
    }

    public boolean remove(IWidget child) {
        if (this.children.remove(child)) {
            child.dispose();
            onChildRemove(child);
            return true;
        }
        return false;
    }

    public boolean remove(int index) {
        if (index < 0) {
            index = getChildren().size() + index + 1;
        }
        IWidget child = this.children.remove(index);
        child.dispose();
        onChildRemove(child);
        return true;
    }

    public void onChildAdd(IWidget child) {
    }

    public void onChildRemove(IWidget child) {
    }

    public W child(IWidget child) {
        if (!addChild(child, -1)) {
            throw new IllegalStateException("Failed to add child");
        }
        return getThis();
    }

    public W childIf(boolean condition, IWidget child) {
        if (condition) return child(child);
        return getThis();
    }

    public W childIf(BooleanSupplier condition, IWidget child) {
        if (condition.getAsBoolean()) return child(child);
        return getThis();
    }

    public W childIf(boolean condition, Supplier<IWidget> child) {
        if (condition) return child(child.get());
        return getThis();
    }

    public W childIf(BooleanSupplier condition, Supplier<IWidget> child) {
        if (condition.getAsBoolean()) return child(child.get());
        return getThis();
    }
}
