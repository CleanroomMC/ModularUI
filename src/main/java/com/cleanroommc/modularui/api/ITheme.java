package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.theme.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A theme is parsed from json and contains style information like color or background texture.
 */
@SideOnly(Side.CLIENT)
public interface ITheme {

    /**
     * @return the master default theme.
     */
    static ITheme getDefault() {
        return Theme.DEFAULT_DEFAULT;
    }

    /**
     * @param id theme id
     * @return theme with given id
     */
    static ITheme get(String id) {
        return ThemeManager.get(id);
    }

    /**
     * If you have a custom widget type and want a custom theme for the type you can register a parser here.
     *
     * @param id           the id of the widget theme. This how json finds this parser.
     * @param defaultTheme this will be used for {@link Theme#DEFAULT_DEFAULT}.
     * @param function     a function that creates an instance of the widget theme with the parent theme and a json.
     */
    static void registerWidgetTheme(String id, WidgetTheme defaultTheme, WidgetThemeParser function) {
        ThemeManager.registerWidgetTheme(id, defaultTheme, function);
    }

    /**
     * Register a default theme for a screen id. Can also just be the mod from many screens.
     *
     * @param screenId screen id or mod.
     * @param theme    theme
     */
    static void registerDefaultScreenTheme(String screenId, ITheme theme) {
        ThemeManager.registerDefaultTheme(screenId, theme.getId());
    }

    /**
     * Register a default theme for a screen id.
     *
     * @param mod   mod id of screen
     * @param id    screen id without mod
     * @param theme theme
     */
    static void registerDefaultScreenTheme(String mod, String id, ITheme theme) {
        ThemeManager.registerDefaultTheme(mod + ":" + id, theme.getId());
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
