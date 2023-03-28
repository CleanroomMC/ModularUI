package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.google.gson.JsonObject;

public class WidgetTextFieldTheme extends WidgetTheme {

    private final int markedColor;

    public WidgetTextFieldTheme(int markedColor) {
        super(GuiTextures.DISPLAY, null, Color.WHITE.normal, Color.WHITE.normal, false);
        this.markedColor = markedColor;
    }

    public WidgetTextFieldTheme(WidgetTheme parent, WidgetTheme fallback, JsonObject json, boolean fallbackToParent) {
        super(parent, fallback, json, fallbackToParent);
        this.markedColor = JsonHelper.getColor(json, ((WidgetTextFieldTheme) parent).getMarkedColor(), "markedColor");
    }

    public int getMarkedColor() {
        return markedColor;
    }
}
