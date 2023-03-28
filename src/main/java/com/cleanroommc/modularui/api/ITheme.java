package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.theme.*;

public interface ITheme {

    static ITheme getDefault() {
        return Theme.DEFAULT_DEFAULT;
    }

    static ITheme get(String id) {
        return ThemeHandler.get(id);
    }

    String getId();

    ITheme getParentTheme();

    WidgetTheme getFallback();

    WidgetTheme getPanelTheme();

    WidgetTheme getButtonTheme();

    WidgetSlotTheme getItemSlotTheme();

    WidgetSlotTheme getFluidSlotTheme();

    WidgetTextFieldTheme getTextFieldTheme();

    WidgetTheme getWidgetTheme(String id);

}
