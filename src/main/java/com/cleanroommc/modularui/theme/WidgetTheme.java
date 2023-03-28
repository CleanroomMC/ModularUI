package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class WidgetTheme {

    @Nullable
    private final IDrawable background;
    @Nullable
    private final IDrawable hoverBackground;
    private final int color;
    private final int textColor;
    private final boolean textShadow;

    public WidgetTheme(@Nullable IDrawable background, @Nullable IDrawable hoverBackground, int color, int textColor, boolean textShadow) {
        this.background = background;
        this.hoverBackground = hoverBackground;
        this.color = color;
        this.textColor = textColor;
        this.textShadow = textShadow;
    }

    public WidgetTheme(WidgetTheme parent, WidgetTheme fallback, JsonObject json, boolean fallbackToParent) {
        this.background = JsonHelper.deserialize(json, IDrawable.class, fallbackToParent ? parent.getBackground() : fallback.getBackground(), "background", "bg");
        this.hoverBackground = JsonHelper.deserialize(json, IDrawable.class, fallbackToParent ? parent.getHoverBackground() : fallback.getHoverBackground(), "hoverBackground", "hbg");
        this.color = JsonHelper.getColor(json, fallbackToParent ? parent.getColor() : fallback.getColor(), "color");
        this.textColor = JsonHelper.getColor(json, fallbackToParent ? parent.getTextColor() : fallback.getTextColor(), "textColor");
        this.textShadow = JsonHelper.getBoolean(json, fallbackToParent ? parent.getTextShadow() : fallback.getTextShadow(), "textShadow");
    }

    public @Nullable IDrawable getBackground() {
        return background;
    }

    public @Nullable IDrawable getHoverBackground() {
        return hoverBackground;
    }

    public int getColor() {
        return color;
    }

    public int getTextColor() {
        return textColor;
    }

    public boolean getTextShadow() {
        return textShadow;
    }
}
