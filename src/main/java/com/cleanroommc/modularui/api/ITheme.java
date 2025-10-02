package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.theme.SelectableTheme;
import com.cleanroommc.modularui.theme.SlotTheme;
import com.cleanroommc.modularui.theme.TextFieldTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.theme.WidgetThemeKey;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

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

    @UnmodifiableView
    Collection<WidgetThemeEntry<?>> getWidgetThemes();

    WidgetThemeEntry<WidgetTheme> getFallback();

    WidgetThemeEntry<WidgetTheme> getPanelTheme();

    WidgetThemeEntry<WidgetTheme> getButtonTheme();

    WidgetThemeEntry<SlotTheme> getItemSlotTheme();

    WidgetThemeEntry<SlotTheme> getFluidSlotTheme();

    WidgetThemeEntry<TextFieldTheme> getTextFieldTheme();

    WidgetThemeEntry<SelectableTheme> getToggleButtonTheme();

    <T extends WidgetTheme> WidgetThemeEntry<T> getWidgetTheme(WidgetThemeKey<T> key);
}
