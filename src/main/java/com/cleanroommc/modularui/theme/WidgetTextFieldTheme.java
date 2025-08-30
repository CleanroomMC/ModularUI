package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class WidgetTextFieldTheme extends WidgetTheme {

    private final int markedColor;
    private final int hintColor;

    public WidgetTextFieldTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background, @Nullable IDrawable hoverBackground,
                                int color, int textColor, boolean textShadow, int markedColor, int hintColor) {
        super(defaultWidth, defaultHeight, background, hoverBackground, color, textColor, textShadow);
        this.markedColor = markedColor;
        this.hintColor = hintColor;
    }

    public WidgetTextFieldTheme(int markedColor, int hintColor) {
        this(56, 18, GuiTextures.DISPLAY_SMALL, null, Color.WHITE.main, Color.WHITE.main, false, markedColor, hintColor);
    }

    public WidgetTextFieldTheme(WidgetTheme parent, JsonObject fallback, JsonObject json) {
        super(parent, json, fallback);
        this.markedColor = JsonHelper.getColorWithFallback(json, fallback, ((WidgetTextFieldTheme) parent).getMarkedColor(), IThemeApi.MARKED_COLOR);
        this.hintColor = JsonHelper.getColorWithFallback(json, fallback, ((WidgetTextFieldTheme) parent).getHintColor(), IThemeApi.HINT_COLOR);
    }

    public int getMarkedColor() {
        return this.markedColor;
    }

    public int getHintColor() {
        return hintColor;
    }
}
