package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonObject;

public class WidgetSlotTheme extends WidgetTheme {

    private final int slotHoverColor;

    public WidgetSlotTheme(IDrawable background, int slotHoverColor) {
        super(background, null, Color.WHITE.main, 0xFF404040, false);
        this.slotHoverColor = slotHoverColor;
    }

    public WidgetSlotTheme(WidgetTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        this.slotHoverColor = JsonHelper.getColorWithFallback(json, fallback, ((WidgetSlotTheme) parent).getSlotHoverColor(), "slotHoverColor");
    }

    public int getSlotHoverColor() {
        return this.slotHoverColor;
    }
}
