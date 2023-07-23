package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.JsonBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ThemeAPI implements IThemeApi {

    public static final ThemeAPI INSTANCE = new ThemeAPI();
    public static final String DEFAULT = "DEFAULT";
    public static final ITheme DEFAULT_DEFAULT = new DefaultTheme(INSTANCE);

    private final Map<String, ITheme> THEMES = new Object2ObjectOpenHashMap<>();
    protected final Map<String, List<JsonBuilder>> defaultThemes = new Object2ObjectOpenHashMap<>();
    protected final Map<String, WidgetTheme> defaultWidgetThemes = new Object2ObjectOpenHashMap<>();
    protected final Map<String, WidgetThemeParser> widgetThemeFunctions = new Object2ObjectOpenHashMap<>();
    protected final Map<String, String> jsonScreenThemes = new Object2ObjectOpenHashMap<>();
    private final Map<String, String> screenThemes = new Object2ObjectOpenHashMap<>();

    private ThemeAPI() {
        registerWidgetTheme(Theme.PANEL, new WidgetTheme(GuiTextures.BACKGROUND, null, Color.WHITE.normal, 0xFF404040, false), (parent, json, fallback) -> new WidgetTheme(parent, fallback, json));
        registerWidgetTheme(Theme.BUTTON, new WidgetTheme(GuiTextures.BUTTON, null, Color.WHITE.normal, Color.WHITE.normal, true), (parent, json, fallback) -> new WidgetTheme(parent, fallback, json));
        registerWidgetTheme(Theme.ITEM_SLOT, new WidgetSlotTheme(GuiTextures.SLOT, Color.withAlpha(Color.WHITE.normal, 0x60)), (parent, json, fallback) -> new WidgetSlotTheme(parent, fallback, json));
        registerWidgetTheme(Theme.FLUID_SLOT, new WidgetSlotTheme(GuiTextures.SLOT_DARK, Color.withAlpha(Color.WHITE.normal, 0x60)), (parent, json, fallback) -> new WidgetSlotTheme(parent, fallback, json));
        registerWidgetTheme(Theme.TEXT_FIELD, new WidgetTextFieldTheme(0xFF2F72A8), (parent, json, fallback) -> new WidgetTextFieldTheme(parent, fallback, json));
    }

    @Override
    public ITheme getDefaultTheme() {
        return DEFAULT_DEFAULT;
    }

    @Override
    public @NotNull ITheme getTheme(String id) {
        return this.THEMES.getOrDefault(id, getDefaultTheme());
    }

    @Override
    public boolean hasTheme(String id) {
        return this.THEMES.containsKey(id);
    }

    @Override
    public void registerTheme(String id, JsonBuilder json) {
        getJavaDefaultThemes(id).add(json);
    }

    @Override
    public List<JsonBuilder> getJavaDefaultThemes(String id) {
        return this.defaultThemes.computeIfAbsent(id, key -> new ArrayList<>());
    }

    @Override
    public ITheme getThemeForScreen(String owner, String name, @Nullable String defaultTheme) {
        String theme = getThemeIdForScreen(owner, name);
        if (theme != null) return getTheme(theme);
        if (defaultTheme != null) return getTheme(defaultTheme);
        return getTheme(ModularUIConfig.useDarkThemeByDefault ? "vanilla_dark" : "vanilla");
    }

    private String getThemeIdForScreen(String mod, String name) {
        String theme = this.jsonScreenThemes.get(mod + ":" + name);
        if (theme != null) return theme;
        theme = this.jsonScreenThemes.get(mod);
        if (theme != null) return theme;
        theme = this.screenThemes.get(mod + ":" + name);
        return theme;
    }

    @Override
    public void registerThemeForScreen(String screen, String theme) {
        Objects.requireNonNull(screen);
        Objects.requireNonNull(theme);
        this.screenThemes.put(screen, theme);
    }

    @Override
    public void registerWidgetTheme(String id, WidgetTheme defaultTheme, WidgetThemeParser parser) {
        if (this.widgetThemeFunctions.containsKey(id)) {
            throw new IllegalStateException();
        }
        this.widgetThemeFunctions.put(id, parser);
        this.defaultWidgetThemes.put(id, defaultTheme);
    }

    // Internals

    @ApiStatus.Internal
    void registerTheme(ITheme theme) {
        if (this.THEMES.containsKey(theme.getId())) {
            throw new IllegalArgumentException("Theme with id " + theme.getId() + " already exists!");
        }
        this.THEMES.put(theme.getId(), theme);
    }

    @ApiStatus.Internal
    void onReload() {
        this.THEMES.clear();
        this.jsonScreenThemes.clear();
        registerTheme(DEFAULT_DEFAULT);
    }

    public static class DefaultTheme extends AbstractDefaultTheme {

        private final ThemeAPI api;

        private DefaultTheme(ThemeAPI api) {
            this.api = api;
        }

        @Override
        public String getId() {
            return DEFAULT;
        }

        @Override
        public WidgetTheme getFallback() {
            return ThemeManager.defaultdefaultWidgetTheme;
        }

        @Override
        public WidgetTheme getWidgetTheme(String id) {
            return this.api.defaultWidgetThemes.get(id);
        }
    }
}
