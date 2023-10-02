package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class WidgetToggleButtonTheme extends WidgetTheme {

    @Nullable
    private final IDrawable disabledBackground;
    @Nullable
    private final IDrawable disabledHoverBackground;
    private final int disabledColor;

    public WidgetToggleButtonTheme(@Nullable IDrawable background, @Nullable IDrawable hoverBackground,
                                   int color, int textColor, boolean textShadow,
                                   @Nullable IDrawable disabledBackground, @Nullable IDrawable disabledHoverBackground,
                                   int disabledColor) {
        super(background, hoverBackground, color, textColor, textShadow);
        this.disabledBackground = disabledBackground;
        this.disabledHoverBackground = disabledHoverBackground;
        this.disabledColor = disabledColor;
    }

    public WidgetToggleButtonTheme(WidgetTheme parent, JsonObject fallback, JsonObject json) {
        super(parent, fallback, json);
        this.disabledBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parent.getBackground(), "disabledBackground");
        this.disabledHoverBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parent.getHoverBackground(), "disabledHoverBackground");
        this.disabledColor = JsonHelper.getColorWithFallback(json, fallback, parent.getColor(), "disabledColor");
    }

    public @Nullable IDrawable getDisabledBackground() {
        return this.disabledBackground;
    }

    public @Nullable IDrawable getDisabledHoverBackground() {
        return this.disabledHoverBackground;
    }

    public int getDisabledColor() {
        return this.disabledColor;
    }
}
