package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class SlotTheme extends WidgetTheme {

    private final int slotHoverColor;

    public SlotTheme(IDrawable background) {
        this(background, Color.withAlpha(Color.WHITE.main, 0x60));
    }

    public SlotTheme(IDrawable background, int slotHoverColor) {
        this(18, 18, background, Color.WHITE.main, 0xFF404040, false, Color.WHITE.main, slotHoverColor);
    }

    public SlotTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background,
                     int color, int textColor, boolean textShadow, int iconColor, int slotHoverColor) {
        super(defaultWidth, defaultHeight, background, color, textColor, textShadow, iconColor);
        this.slotHoverColor = slotHoverColor;
    }

    public SlotTheme(SlotTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        this.slotHoverColor = JsonHelper.getColorWithFallback(json, fallback, parent.getSlotHoverColor(), IThemeApi.SLOT_HOVER_COLOR);
    }

    public int getSlotHoverColor() {
        return this.slotHoverColor;
    }

    public static class Builder<T extends SlotTheme, B extends Builder<T, B>> extends WidgetThemeBuilder<T, B> {

        public B hoverColor(int hoverColor) {
            add(IThemeApi.SLOT_HOVER_COLOR, hoverColor);
            return getThis();
        }
    }
}
