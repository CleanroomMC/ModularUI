package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DelegateDrawable implements IDrawable {

    @NotNull
    private IDrawable drawable;

    public DelegateDrawable(@Nullable IDrawable drawable) {
        setDrawable(drawable);
    }

    // protected, so subclasses can define mutability
    protected void setDrawable(@Nullable IDrawable drawable) {
        this.drawable = drawable != null ? drawable : IDrawable.EMPTY;
    }

    @NotNull
    public IDrawable getWrappedDrawable() {
        return drawable;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        this.drawable.draw(context, x, y, width, height, widgetTheme);
    }

    @Override
    public boolean canApplyTheme() {
        return this.drawable.canApplyTheme();
    }

    @Override
    public void applyColor(int themeColor) {
        this.drawable.applyColor(themeColor);
    }

    @Override
    public int getDefaultWidth() {
        return this.drawable.getDefaultWidth();
    }

    @Override
    public int getDefaultHeight() {
        return this.drawable.getDefaultHeight();
    }

    @Override
    public Widget<?> asWidget() {
        return this.drawable.asWidget();
    }

    @Override
    public Icon asIcon() {
        return this.drawable.asIcon();
    }
}
