package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonObject;

public class WidgetSlotTheme extends WidgetTheme {

    private final int slotHoverColor;

    public WidgetSlotTheme(IDrawable background, IDrawable hoverBackground, int slotHoverColor) {
        super(background, hoverBackground, Color.WHITE.normal, 0xFF404040, false);
        this.slotHoverColor = slotHoverColor;
    }

    public WidgetSlotTheme(WidgetTheme parent, JsonObject json) {
        super(parent, json);
        this.slotHoverColor = JsonHelper.getColor(json, ((WidgetSlotTheme) parent).getSlotHoverColor(), "slotHoverColor");
    }

    public int getSlotHoverColor() {
        return slotHoverColor;
    }
}
