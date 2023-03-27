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

    public WidgetTheme(WidgetTheme parent, JsonObject json) {
        this.background = JsonHelper.deserialize(json, IDrawable.class, parent.getBackground(), "background", "bg");
        this.hoverBackground = JsonHelper.deserialize(json, IDrawable.class, parent.getHoverBackground(), "hoverBackground", "hbg");
        this.color = JsonHelper.getColor(json, parent.getColor(), "color");
        this.textColor = JsonHelper.getColor(json, parent.getTextColor(), "textColor");
        this.textShadow = JsonHelper.getBoolean(json, parent.getTextShadow(), "textShadow");
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
