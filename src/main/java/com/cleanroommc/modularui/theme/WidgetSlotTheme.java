package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WidgetSlotTheme extends WidgetTheme {

    private final int slotHoverColor;

    public WidgetSlotTheme(IDrawable background, int slotHoverColor) {
        super(background, null, Color.WHITE.normal, 0xFF404040, false);
        this.slotHoverColor = slotHoverColor;
    }

    public WidgetSlotTheme(WidgetTheme parent, JsonObject fallback, JsonObject json) {
        super(parent, fallback, json);
        this.slotHoverColor = JsonHelper.getColorWithFallback(json, fallback, ((WidgetSlotTheme) parent).getSlotHoverColor(), "slotHoverColor");
    }

    public int getSlotHoverColor() {
        return slotHoverColor;
    }
}
