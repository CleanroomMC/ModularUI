package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class WidgetToggleButtonTheme extends WidgetTheme {

    @Nullable
    private final IDrawable selectedBackground;
    @Nullable
    private final IDrawable selectedHoverBackground;
    private final int selectedColor;

    public WidgetToggleButtonTheme(@Nullable IDrawable background, @Nullable IDrawable hoverBackground,
                                   int color, int textColor, boolean textShadow,
                                   @Nullable IDrawable selectedBackground, @Nullable IDrawable selectedHoverBackground,
                                   int selectedColor) {
        super(background, hoverBackground, color, textColor, textShadow);
        this.selectedBackground = selectedBackground;
        this.selectedHoverBackground = selectedHoverBackground;
        this.selectedColor = selectedColor;
    }

    public WidgetToggleButtonTheme(WidgetTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        WidgetToggleButtonTheme parentToggleButtonTheme = (WidgetToggleButtonTheme) parent;
        this.selectedBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parentToggleButtonTheme.getSelectedBackground(), "selectedBackground");
        this.selectedHoverBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parentToggleButtonTheme.getSelectedHoverBackground(), "selectedHoverBackground");
        this.selectedColor = JsonHelper.getColorWithFallback(json, fallback, parentToggleButtonTheme.getSelectedColor(), "selectedColor");
    }

    public @Nullable IDrawable getSelectedBackground() {
        return this.selectedBackground;
    }

    public @Nullable IDrawable getSelectedHoverBackground() {
        return this.selectedHoverBackground;
    }

    public int getSelectedColor() {
        return this.selectedColor;
    }
}
