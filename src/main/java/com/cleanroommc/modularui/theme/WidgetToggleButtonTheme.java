package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class WidgetToggleButtonTheme extends WidgetTheme {

    private final WidgetTheme selected;

    public WidgetToggleButtonTheme(@Nullable IDrawable background, @Nullable IDrawable hoverBackground,
                                   int color, int textColor, boolean textShadow,
                                   @Nullable IDrawable selectedBackground, @Nullable IDrawable selectedHoverBackground,
                                   int selectedColor, int selectedTextColor, boolean selectedTextShadow) {
        super(background, hoverBackground, color, textColor, textShadow);
        this.selected = new WidgetTheme(selectedBackground, selectedHoverBackground, selectedColor, selectedTextColor, selectedTextShadow);
    }

    public WidgetToggleButtonTheme(WidgetTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        WidgetToggleButtonTheme parentWTBT = (WidgetToggleButtonTheme) parent;
        IDrawable selectedBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parentWTBT.getSelected().getBackground(), "selectedBackground");
        IDrawable selectedHoverBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parentWTBT.getSelected().getHoverBackground(), "selectedHoverBackground");
        int selectedColor = JsonHelper.getColorWithFallback(json, fallback, parentWTBT.getSelected().getColor(), "selectedColor");
        int selectedTextColor = JsonHelper.getColorWithFallback(json, fallback, parentWTBT.getSelected().getTextColor(), "selectedTextColor");
        boolean selectedTextShadow = JsonHelper.getBoolWithFallback(json, fallback, parentWTBT.getSelected().getTextShadow(), "selectedTextShadow");
        this.selected = new WidgetTheme(selectedBackground, selectedHoverBackground, selectedColor, selectedTextColor, selectedTextShadow);
    }

    public WidgetTheme getSelected() {
        return selected;
    }
}
