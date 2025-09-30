package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.sizer.Box;

public class DelegateIcon implements IIcon {

    private IIcon icon;

    public DelegateIcon(IIcon icon) {
        this.icon = icon;
    }

    @Override
    public IIcon getWrappedDrawable() {
        return icon;
    }

    @Override
    public int getWidth() {
        return this.icon.getWidth();
    }

    @Override
    public int getHeight() {
        return this.icon.getHeight();
    }

    @Override
    public Box getMargin() {
        return this.icon.getMargin();
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        this.icon.draw(context, x, y, width, height, widgetTheme);
    }

    public IIcon getDelegate() {
        return icon;
    }

    public IIcon findRootDelegate() {
        IIcon icon = this;
        while (icon instanceof DelegateIcon di) {
            icon = di.getDelegate();
        }
        return icon;
    }

    protected void setDelegate(IIcon icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + this.icon + ")";
    }
}
