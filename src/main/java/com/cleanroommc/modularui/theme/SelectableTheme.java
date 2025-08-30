package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class SelectableTheme extends WidgetTheme {

    private final WidgetTheme selected;

    public SelectableTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background, @Nullable IDrawable hoverBackground,
                           int color, int textColor, boolean textShadow, int iconColor,
                           @Nullable IDrawable selectedBackground, @Nullable IDrawable selectedHoverBackground,
                           int selectedColor, int selectedTextColor, boolean selectedTextShadow, int selectedIconColor) {
        super(defaultWidth, defaultHeight, background, hoverBackground, color, textColor, textShadow, iconColor);
        this.selected = new WidgetTheme(defaultWidth, defaultHeight, selectedBackground, selectedHoverBackground, selectedColor, selectedTextColor, selectedTextShadow, selectedIconColor);
    }

    public SelectableTheme(SelectableTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        IDrawable selectedBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parent.getSelected().getBackground(), IThemeApi.SELECTED_BACKGROUND);
        IDrawable selectedHoverBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parent.getSelected().getHoverBackground(), IThemeApi.SELECTED_HOVER_BACKGROUND);
        int selectedColor = JsonHelper.getColorWithFallback(json, fallback, parent.getSelected().getColor(), IThemeApi.SELECTED_COLOR);
        int selectedTextColor = JsonHelper.getColorWithFallback(json, fallback, parent.getSelected().getTextColor(), IThemeApi.SELECTED_TEXT_COLOR);
        boolean selectedTextShadow = JsonHelper.getBoolWithFallback(json, fallback, parent.getSelected().getTextShadow(), IThemeApi.SELECTED_TEXT_SHADOW);
        int selectedIconColor = JsonHelper.getColorWithFallback(json, fallback, parent.getSelected().getTextColor(), IThemeApi.SELECTED_ICON_COLOR);
        this.selected = new WidgetTheme(getDefaultWidth(), getDefaultHeight(), selectedBackground, selectedHoverBackground, selectedColor, selectedTextColor, selectedTextShadow, selectedIconColor);
    }

    public WidgetTheme getSelected() {
        return selected;
    }
}
