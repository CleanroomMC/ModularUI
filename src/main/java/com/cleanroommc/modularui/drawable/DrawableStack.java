package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.IJsonSerializable;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import com.google.gson.JsonObject;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A stack of {@link IDrawable} backed by an array which are drawn on top of each other.
 */
public class DrawableStack implements IDrawable, IJsonSerializable {

    public static final IDrawable[] EMPTY_BACKGROUND = {};

    private final IDrawable[] drawables;

    public DrawableStack(IDrawable... drawables) {
        this.drawables = drawables == null || drawables.length == 0 ? EMPTY_BACKGROUND : drawables;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        for (IDrawable drawable : this.drawables) {
            if (drawable != null) drawable.draw(context, x, y, width, height, widgetTheme);
        }
    }

    @Override
    public boolean canApplyTheme() {
        for (IDrawable drawable : this.drawables) {
            if (drawable != null && drawable.canApplyTheme()) {
                return true;
            }
        }
        return false;
    }

    public IDrawable[] getDrawables() {
        return this.drawables;
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        // serialized as special case
        // this method should never be called
        throw new IllegalStateException();
    }
}
