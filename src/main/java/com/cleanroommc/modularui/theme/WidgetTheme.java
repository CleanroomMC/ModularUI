package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class WidgetTheme {

    public static WidgetThemeEntry<WidgetTheme> getDefault() {
        return ThemeAPI.DEFAULT_THEME.getFallback();
    }

    private final int defaultWidth;
    private final int defaultHeight;
    @Nullable
    private final IDrawable background;
    private final int color;
    private final int textColor;
    private final boolean textShadow;
    private final int iconColor;

    public static WidgetTheme whiteTextShadow(int defaultWidth, int defaultHeight, @Nullable IDrawable background) {
        return new WidgetTheme(defaultWidth, defaultHeight, background, Color.WHITE.main, Color.WHITE.main, true, Color.WHITE.main);
    }

    public static WidgetTheme darkTextNoShadow(int defaultWidth, int defaultHeight, @Nullable IDrawable background) {
        return new WidgetTheme(defaultWidth, defaultHeight, background, Color.WHITE.main, Color.TEXT_COLOR_DARK, false, Color.WHITE.main);
    }

    public WidgetTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background,
                       int color, int textColor, boolean textShadow, int iconColor) {
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.background = background;
        this.color = color;
        this.textColor = textColor == 0 ? color : textColor;
        this.textShadow = textShadow;
        this.iconColor = iconColor == 0 ? color : iconColor;
    }

    public WidgetTheme(WidgetTheme parent, JsonObject json, JsonObject fallback) {
        this.defaultWidth = JsonHelper.getInt(json, parent.getDefaultWidth(), "w", "width");
        this.defaultHeight = JsonHelper.getInt(json, parent.getDefaultHeight(), "h", "height");
        this.background = JsonHelper.deserialize(json, IDrawable.class, parent.getBackground(), IThemeApi.BACKGROUND, "bg");
        // color, textColor, textShadow and iconColor inherit from fallback first and then from parent widget theme
        this.color = JsonHelper.getColorWithFallback(json, inherits(json, IThemeApi.COLOR) ? null : fallback, parent.getColor(), IThemeApi.COLOR);
        int textColor = JsonHelper.getColorWithFallback(json, inherits(json, IThemeApi.TEXT_COLOR) ? null : fallback, parent.getTextColor(), IThemeApi.TEXT_COLOR);
        this.textColor = textColor == 0 ? color : textColor;
        this.textShadow = JsonHelper.getBoolWithFallback(json, inherits(json, IThemeApi.TEXT_SHADOW) ? null : fallback, parent.getTextShadow(), IThemeApi.TEXT_SHADOW);
        int iconColor = JsonHelper.getColorWithFallback(json, inherits(json, IThemeApi.ICON_COLOR) ? null : fallback, parent.getIconColor(), IThemeApi.ICON_COLOR);
        this.iconColor = iconColor == 0 ? color : iconColor;
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

    public int getColor() {
        return this.color;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public boolean getTextShadow() {
        return this.textShadow;
    }

    public int getIconColor() {
        return iconColor;
    }

    public WidgetTheme withColor(int color) {
        return new WidgetTheme(this.defaultWidth, this.defaultHeight, this.background, color, this.textColor, this.textShadow, this.iconColor);
    }
}
