package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import java.util.function.Supplier;

/**
 * Takes supplier of {@link IDrawable} and draws conditional drawable.
 * Return value of the supplier should be deterministic per render frame,
 * in order to apply {@link ITheme} to correct object.
 */
public class DynamicDrawable implements IDrawable {

    private final Supplier<IDrawable> supplier;

    public DynamicDrawable(Supplier<IDrawable> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height) {
        IDrawable drawable = supplier.get();
        if (drawable != null) {
            drawable.draw(context, x, y, width, height);
        }
    }

    @Override
    public void applyThemeColor(ITheme theme, WidgetTheme widgetTheme) {
        IDrawable drawable = supplier.get();
        if (drawable != null) {
            drawable.applyThemeColor(theme, widgetTheme);
        } else {
            IDrawable.super.applyThemeColor(theme, widgetTheme);
        }
    }

    @Override
    public boolean canApplyTheme() {
        IDrawable drawable = supplier.get();
        if (drawable != null) {
            return drawable.canApplyTheme();
        } else {
            return IDrawable.super.canApplyTheme();
        }
    }

    public Supplier<IDrawable> getSupplier() {
        return supplier;
    }
}
