package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTextFieldTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;

/**
 * A theme is parsed from json and contains style information like color or background texture.
 */
public interface ITheme {

    /**
     * @return the master default theme.
     */
    static ITheme getDefault() {
        return IThemeApi.get().getDefaultTheme();
    }

    /**
     * @param id theme id
     * @return theme with given id
     */
    static ITheme get(String id) {
        return IThemeApi.get().getTheme(id);
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
