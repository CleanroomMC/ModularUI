package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DrawableArray implements IDrawable {

    public static final IDrawable[] EMPTY_BACKGROUND = {};

    private final IDrawable[] drawables;
    private WidgetTheme currentWidgetTheme;

    public DrawableArray(IDrawable... drawables) {
        this.drawables = drawables == null || drawables.length == 0 ? EMPTY_BACKGROUND : drawables;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height) {
        for (IDrawable drawable : this.drawables) {
            if (this.currentWidgetTheme != null) {
                drawable.applyThemeColor(context.getTheme(), this.currentWidgetTheme);
            }
            drawable.draw(context, x, y, width, height);
        }
        this.currentWidgetTheme = null;
    }

    @Override
    public void applyThemeColor(ITheme theme, WidgetTheme widgetTheme) {
        this.currentWidgetTheme = widgetTheme;
    }

    @Override
    public boolean canApplyTheme() {
        for (IDrawable drawable : this.drawables) {
            if (drawable.canApplyTheme()) {
                return true;
            }
        }
        return false;
    }

    public IDrawable[] getDrawables() {
        return this.drawables;
    }
}
