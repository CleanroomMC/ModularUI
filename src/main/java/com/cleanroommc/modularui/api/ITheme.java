package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTextFieldTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeSelectable;

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

    /**
     * @return theme id
     */
    String getId();

    /**
     * @return parent theme
     */
    ITheme getParentTheme();

    WidgetTheme getFallback();

    WidgetTheme getPanelTheme();

    WidgetTheme getButtonTheme();

    WidgetSlotTheme getItemSlotTheme();

    WidgetSlotTheme getFluidSlotTheme();

    WidgetTextFieldTheme getTextFieldTheme();

    WidgetThemeSelectable getToggleButtonTheme();

    WidgetTheme getWidgetTheme(String id);

    default <T extends WidgetTheme> T getWidgetTheme(Class<T> clazz, String id) {
        WidgetTheme theme = getWidgetTheme(id);
        if (clazz.isInstance(theme)) {
            return (T) theme;
        }
        return null;
    }

    int getOpenCloseAnimationOverride();

    boolean getSmoothProgressBarOverride();

    Tooltip.Pos getTooltipPosOverride();
}
