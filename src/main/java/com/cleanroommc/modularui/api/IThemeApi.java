package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.theme.SelectableTheme;
import com.cleanroommc.modularui.theme.SlotTheme;
import com.cleanroommc.modularui.theme.TextFieldTheme;
import com.cleanroommc.modularui.theme.ThemeAPI;
import com.cleanroommc.modularui.theme.ThemeBuilder;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeKey;
import com.cleanroommc.modularui.theme.WidgetThemeParser;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonBuilder;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An API interface for Themes.
 */
@ApiStatus.NonExtendable
public interface IThemeApi {

    // widget themes
    WidgetThemeKey<WidgetTheme> FALLBACK = get().registerWidgetTheme("default",
            new WidgetTheme(18, 18, null, null, Color.WHITE.main, 0xFF404040, false, Color.WHITE.main),
            WidgetTheme::new);
    WidgetThemeKey<WidgetTheme> PANEL = get().registerWidgetTheme("panel",
            new WidgetTheme(176, 166, GuiTextures.MC_BACKGROUND, null, Color.WHITE.main, 0xFF404040, false, Color.WHITE.main),
            WidgetTheme::new);
    WidgetThemeKey<WidgetTheme> BUTTON = get().registerWidgetTheme("button",
            new WidgetTheme(18, 18, GuiTextures.MC_BUTTON, GuiTextures.MC_BUTTON_HOVERED, Color.WHITE.main, Color.WHITE.main, true, Color.WHITE.main),
            WidgetTheme::new);
    WidgetThemeKey<SlotTheme> ITEM_SLOT = get().registerWidgetTheme("itemSlot",
            new SlotTheme(GuiTextures.SLOT_ITEM, Color.withAlpha(Color.WHITE.main, 0x60)), SlotTheme::new);
    WidgetThemeKey<SlotTheme> FLUID_SLOT = get().registerWidgetTheme("fluidSlot",
            new SlotTheme(GuiTextures.SLOT_FLUID, Color.withAlpha(Color.WHITE.main, 0x60)), SlotTheme::new);
    WidgetThemeKey<TextFieldTheme> TEXT_FIELD = get().registerWidgetTheme("textField",
            new TextFieldTheme(0xFF2F72A8, 0xFF5F5F5F),
            (parent, json, fallback) -> new TextFieldTheme(parent, fallback, json));
    WidgetThemeKey<SelectableTheme> TOGGLE_BUTTON = get().registerWidgetTheme("toggleButton",
            new SelectableTheme(18, 18, GuiTextures.MC_BUTTON, GuiTextures.MC_BUTTON_HOVERED, Color.WHITE.main, Color.WHITE.main, true,
                    Color.WHITE.main, GuiTextures.MC_BUTTON_DISABLED, IDrawable.NONE, Color.WHITE.main, Color.WHITE.main, true, Color.WHITE.main),
            SelectableTheme::new);

    // sub widget themes
    WidgetThemeKey<SlotTheme> ITEM_SLOT_PLAYER = ITEM_SLOT.createSubKey("player");

    // properties
    String PARENT = "parent";
    String DEFAULT_WIDTH = "defaultWidth";
    String DEFAULT_HEIGHT = "defaultHeight";
    String BACKGROUND = "background";
    String HOVER_BACKGROUND = "hoverBackground";
    String COLOR = "color";
    String TEXT_COLOR = "textColor";
    String TEXT_SHADOW = "textShadow";
    String ICON_COLOR = "iconColor";
    String SLOT_HOVER_COLOR = "slotHoverColor";
    String MARKED_COLOR = "markedColor";
    String HINT_COLOR = "hintColor";
    String SELECTED_BACKGROUND = "selectedBackground";
    String SELECTED_HOVER_BACKGROUND = "selectedHoverBackground";
    String SELECTED_COLOR = "selectedColor";
    String SELECTED_TEXT_COLOR = "selectedTextColor";
    String SELECTED_TEXT_SHADOW = "selectedTextShadow";
    String SELECTED_ICON_COLOR = "selectedIconColor";

    /**
     * @return the default api implementation
     */
    @Contract(pure = true)
    static IThemeApi get() {
        return ThemeAPI.INSTANCE;
    }

    /**
     * @return the absolute fallback theme
     */
    ITheme getDefaultTheme();

    /**
     * Finds a theme for an id
     *
     * @param id id of the theme
     * @return the found theme or {@link #getDefaultTheme()} if no theme was found
     */
    @NotNull
    ITheme getTheme(String id);

    /**
     * @param id id of the theme
     * @return if a theme with the id is registered
     */
    boolean hasTheme(String id);

    /**
     * @param key id of the widget theme
     * @return if a widget theme with the id is registered
     */
    boolean hasWidgetTheme(WidgetThemeKey<?> key);

    /**
     * Registers a theme json object. Themes from resource packs always have greater priority.
     * Json builders are used here as they are much easier to merge as opposed to normal java objects.
     *
     * @param id   id of the theme
     * @param json theme data
     */
    void registerTheme(String id, JsonBuilder json);

    /**
     * Registers a theme json object. Themes from resource packs always have greater priority.
     *
     * @param themeBuilder theme data
     */
    default void registerTheme(ThemeBuilder<?> themeBuilder) {
        registerTheme(themeBuilder.getId(), themeBuilder);
    }

    /**
     * Gets all currently from java side registered theme json's for a theme.
     *
     * @param id id of the theme
     * @return all theme json's for a theme.
     */
    List<JsonBuilder> getJavaDefaultThemes(String id);

    /**
     * Gets the appropriate theme for a screen.
     *
     * @param owner        owner of the screen
     * @param name         name of the screen
     * @param defaultTheme default theme if no theme was found
     * @return the registered theme for the given screen or the given default theme or {@link #getDefaultTheme()}
     */
    ITheme getThemeForScreen(String owner, String name, @Nullable String defaultTheme);

    /**
     * Gets the appropriate theme for a screen.
     *
     * @param screen       screen
     * @param defaultTheme default theme if no theme was found
     * @return the registered theme for the given screen or the given default theme or {@link #getDefaultTheme()}
     */
    default ITheme getThemeForScreen(ModularScreen screen, @Nullable String defaultTheme) {
        return getThemeForScreen(screen.getOwner(), screen.getName(), defaultTheme);
    }

    /**
     * Registers a theme for a screen. Themes from resource packs always have greater priority.
     *
     * @param owner owner of the screen
     * @param name  name of the screen
     * @param theme theme to register
     */
    default void registerThemeForScreen(String owner, String name, String theme) {
        registerThemeForScreen(owner + ":" + name, theme);
    }

    /**
     * Registers a theme for a screen. Themes from resource packs always have greater priority.
     *
     * @param screen full screen id
     * @param theme  theme to register
     */
    void registerThemeForScreen(String screen, String theme);

    /**
     * Registers a widget theme. It is recommended to store the resulting key in a static variable and make it accessible by public methods.
     *
     * @param id           id of the widget theme
     * @param defaultTheme the fallback widget theme
     * @param parser       the widget theme json parser function. This is usually another constructor.
     * @return key to access the widget theme
     */
    <T extends WidgetTheme> WidgetThemeKey<T> registerWidgetTheme(String id, T defaultTheme, WidgetThemeParser<T> parser);
}
