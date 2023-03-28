package com.cleanroommc.modularui.theme;

import com.google.gson.JsonObject;

public interface WidgetThemeParser {

    WidgetTheme parse(WidgetTheme parent, WidgetTheme fallback, JsonObject json, boolean fallbackToParent);
}
