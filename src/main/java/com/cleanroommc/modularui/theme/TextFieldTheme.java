package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class TextFieldTheme extends WidgetTheme {

    private final int markedColor;
    private final int hintColor;

    public TextFieldTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background, @Nullable IDrawable hoverBackground,
                          int color, int textColor, boolean textShadow, int iconColor, int markedColor, int hintColor) {
        super(defaultWidth, defaultHeight, background, hoverBackground, color, textColor, textShadow, iconColor);
        this.markedColor = markedColor;
        this.hintColor = hintColor;
    }

    public TextFieldTheme(int markedColor, int hintColor) {
        this(56, 18, GuiTextures.DISPLAY_SMALL, null, Color.WHITE.main, Color.WHITE.main, false, Color.WHITE.main, markedColor, hintColor);
    }

    public TextFieldTheme(TextFieldTheme parent, JsonObject fallback, JsonObject json) {
        super(parent, json, fallback);
        this.markedColor = JsonHelper.getColorWithFallback(json, fallback, parent.getMarkedColor(), IThemeApi.MARKED_COLOR);
        this.hintColor = JsonHelper.getColorWithFallback(json, fallback, parent.getHintColor(), IThemeApi.HINT_COLOR);
    }

    public int getMarkedColor() {
        return this.markedColor;
    }

    public int getHintColor() {
        return hintColor;
    }

    public static class Builder<T extends TextFieldTheme, B extends TextFieldTheme.Builder<T, B>> extends WidgetThemeBuilder<T, B> {

        public B markedColor(int markedColor) {
            add(IThemeApi.MARKED_COLOR, markedColor);
            return getThis();
        }

        public B hintColor(int hintColor) {
            add(IThemeApi.HINT_COLOR, hintColor);
            return getThis();
        }
    }
}
