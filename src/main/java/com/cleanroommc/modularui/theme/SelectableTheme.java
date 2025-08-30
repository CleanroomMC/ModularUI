package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class SelectableTheme extends WidgetTheme {

    private final WidgetTheme selected;

    public SelectableTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background, @Nullable IDrawable hoverBackground,
                           int color, int textColor, boolean textShadow,
                           @Nullable IDrawable selectedBackground, @Nullable IDrawable selectedHoverBackground,
                           int selectedColor, int selectedTextColor, boolean selectedTextShadow) {
        super(defaultWidth, defaultHeight, background, hoverBackground, color, textColor, textShadow);
        this.selected = new WidgetTheme(defaultWidth, defaultHeight, selectedBackground, selectedHoverBackground, selectedColor, selectedTextColor, selectedTextShadow);
    }

    public SelectableTheme(WidgetTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        SelectableTheme parentWTBT = (SelectableTheme) parent;
        IDrawable selectedBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parentWTBT.getSelected().getBackground(), IThemeApi.SELECTED_BACKGROUND);
        IDrawable selectedHoverBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parentWTBT.getSelected().getHoverBackground(), IThemeApi.SELECTED_HOVER_BACKGROUND);
        int selectedColor = JsonHelper.getColorWithFallback(json, fallback, parentWTBT.getSelected().getColor(), IThemeApi.SELECTED_COLOR);
        int selectedTextColor = JsonHelper.getColorWithFallback(json, fallback, parentWTBT.getSelected().getTextColor(), IThemeApi.SELECTED_TEXT_COLOR);
        boolean selectedTextShadow = JsonHelper.getBoolWithFallback(json, fallback, parentWTBT.getSelected().getTextShadow(), IThemeApi.SELECTED_TEXT_SHADOW);
        this.selected = new WidgetTheme(getDefaultWidth(), getDefaultHeight(), selectedBackground, selectedHoverBackground, selectedColor, selectedTextColor, selectedTextShadow);
    }

    public WidgetTheme getSelected() {
        return selected;
    }
}
