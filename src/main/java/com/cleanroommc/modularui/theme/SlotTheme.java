package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;

public class SlotTheme extends WidgetTheme {

    private final int slotHoverColor;

    public SlotTheme(IDrawable background, int slotHoverColor) {
        super(18, 18, background, null, Color.WHITE.main, 0xFF404040, false, Color.WHITE.main);
        this.slotHoverColor = slotHoverColor;
    }

    public SlotTheme(SlotTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        this.slotHoverColor = JsonHelper.getColorWithFallback(json, fallback, parent.getSlotHoverColor(), IThemeApi.SLOT_HOVER_COLOR);
    }

    public int getSlotHoverColor() {
        return this.slotHoverColor;
    }
}
