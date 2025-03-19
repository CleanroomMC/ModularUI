package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class WidgetTheme {

    public static WidgetTheme getDefault() {
        return ThemeAPI.DEFAULT_DEFAULT.getFallback();
    }

    @Nullable
    private final IDrawable background;
    @Nullable
    private final IDrawable hoverBackground;
    private final int color;
    private final int textColor;
    private final boolean textShadow;

    public WidgetTheme(@Nullable IDrawable background, @Nullable IDrawable hoverBackground,
                       int color, int textColor, boolean textShadow) {
        this.background = background;
        this.hoverBackground = hoverBackground;
        this.color = color;
        this.textColor = textColor;
        this.textShadow = textShadow;
    }

    public WidgetTheme(WidgetTheme parent, JsonObject json, JsonObject fallback) {
        this.background = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parent.getBackground(), IThemeApi.BACKGROUND, "bg");
        this.hoverBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parent.getHoverBackground(), IThemeApi.HOVER_BACKGROUND, "hbg");
        this.color = JsonHelper.getColorWithFallback(json, fallback, parent.getColor(), IThemeApi.COLOR);
        this.textColor = JsonHelper.getColorWithFallback(json, fallback, parent.getTextColor(), IThemeApi.TEXT_COLOR);
        this.textShadow = JsonHelper.getBoolWithFallback(json, fallback, parent.getTextShadow(), IThemeApi.TEXT_SHADOW);
    }

    public @Nullable IDrawable getBackground() {
        return this.background;
    }

    public @Nullable IDrawable getHoverBackground() {
        return this.hoverBackground;
    }

    public int getColor() {
        return this.color;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public boolean getTextShadow() {
        return this.textShadow;
    }

    public WidgetTheme withColor(int color) {
        // TODO it is currently somewhat difficult to color drawable with a custom color. This is a dirty solution.
        return new WidgetTheme(this.background, this.hoverBackground, color, this.textColor, this.textShadow);
    }
}
