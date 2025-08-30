package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class WidgetTheme {

    public static WidgetTheme getDefault() {
        return ThemeAPI.DEFAULT_THEME.getFallback();
    }

    private final int defaultWidth;
    private final int defaultHeight;
    @Nullable
    private final IDrawable background;
    @Nullable
    private final IDrawable hoverBackground;
    private final int color;
    private final int textColor;
    private final boolean textShadow;

    public WidgetTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background, @Nullable IDrawable hoverBackground,
                       int color, int textColor, boolean textShadow) {
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.background = background;
        this.hoverBackground = hoverBackground;
        this.color = color;
        this.textColor = textColor;
        this.textShadow = textShadow;
    }

    public WidgetTheme(WidgetTheme parent, JsonObject json, JsonObject fallback) {
        this.defaultWidth = JsonHelper.getInt(json, parent.getDefaultWidth(), "w", "width");
        this.defaultHeight = JsonHelper.getInt(json, parent.getDefaultHeight(), "h", "height");
        this.background = JsonHelper.deserialize(json, IDrawable.class, parent.getBackground(), IThemeApi.BACKGROUND, "bg");
        this.hoverBackground = JsonHelper.deserialize(json, IDrawable.class, parent.getHoverBackground(), IThemeApi.HOVER_BACKGROUND, "hbg");
        // color, textColor and textShadow inherit from fallback first and then from parent widget theme
        this.color = JsonHelper.getColorWithFallback(json, inherits(json, IThemeApi.COLOR) ? null : fallback, parent.getColor(), IThemeApi.COLOR);
        this.textColor = JsonHelper.getColorWithFallback(json, inherits(json, IThemeApi.TEXT_COLOR) ? null : fallback, parent.getTextColor(), IThemeApi.TEXT_COLOR);
        this.textShadow = JsonHelper.getBoolWithFallback(json, inherits(json, IThemeApi.TEXT_SHADOW) ? null : fallback, parent.getTextShadow(), IThemeApi.TEXT_SHADOW);
    }

    protected static boolean inherits(JsonObject json, String property) {
        if (!json.has("inherit")) return false;
        JsonElement element = json.get("inherit");
        if (element.isJsonPrimitive()) return element.getAsString().equals(property);
        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive() && e.getAsString().equals(property)) return true;
            }
        }
        return false;
    }

    public int getDefaultWidth() {
        return defaultWidth;
    }

    public int getDefaultHeight() {
        return defaultHeight;
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
        return new WidgetTheme(this.defaultWidth, this.defaultHeight, this.background, this.hoverBackground, color, this.textColor, this.textShadow);
    }
}
