package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.utils.JsonBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class ThemeAPI implements IThemeApi {

    public static final ThemeAPI INSTANCE = new ThemeAPI();
    public static final String DEFAULT_ID = "DEFAULT";
    public static final ITheme DEFAULT_THEME = new DefaultTheme();

    public static final Pattern widgetThemeNamePattern = Pattern.compile("[a-zA-Z0-9_-]+");

    private final Object2ObjectMap<String, ITheme> themes = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectMap<String, List<JsonBuilder>> defaultThemes = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectMap<WidgetThemeKey<?>, WidgetTheme> defaultWidgetThemes = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectMap<WidgetThemeKey<?>, WidgetThemeParser<?>> widgetThemeFunctions = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectOpenHashMap<String, String> jsonScreenThemes = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, String> screenThemes = new Object2ObjectOpenHashMap<>();

    private ThemeAPI() {}

    @Override
    public ITheme getDefaultTheme() {
        return DEFAULT_THEME;
    }

    @Override
    public @NotNull ITheme getTheme(String id) {
        return this.themes.getOrDefault(id, getDefaultTheme());
    }

    @Override
    public boolean hasTheme(String id) {
        return this.themes.containsKey(id);
    }

    @Override
    public boolean hasWidgetTheme(WidgetThemeKey<?> key) {
        return this.widgetThemeFunctions.containsKey(key);
    }

    @Override
    public void registerTheme(String id, JsonBuilder json) {
        List<JsonBuilder> themes = getJavaDefaultThemes(id);
        if (!themes.contains(json)) {
            themes.add(json);
        }
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
        String fullName = mod + ":" + name;
        String theme = this.jsonScreenThemes.get(fullName);
        if (theme != null) return theme;
        theme = this.jsonScreenThemes.get(mod);
        if (theme != null) return theme;
        theme = this.screenThemes.get(fullName);
        return theme != null ? theme : this.screenThemes.get(mod);
    }

    @Override
    public void registerThemeForScreen(String screen, String theme) {
        Objects.requireNonNull(screen);
        Objects.requireNonNull(theme);
        this.screenThemes.put(screen, theme);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends WidgetTheme> WidgetThemeKey<T> registerWidgetTheme(String id, T defaultTheme, WidgetThemeParser<T> parser) {
        Objects.requireNonNull(id, "Id for widget theme must not be null");
        Objects.requireNonNull(defaultTheme, "Default widget theme must not be null, but is null for id '" + id + "'.");
        Objects.requireNonNull(parser, "Parser for widget theme must not be null, but is null for id '" + id + "'.");
        if (this.widgetThemeFunctions.containsKey(id)) {
            throw new IllegalStateException("There already is a widget theme for id '" + id + "' registered.");
        }
        if (!widgetThemeNamePattern.matcher(id).matches()) {
            throw new IllegalArgumentException("Widget theme id '" + id + "' is invalid. Id must only contain letters, numbers, underscores and minus.");
        }
        Class<T> type = (Class<T>) defaultTheme.getClass();
        WidgetThemeKey<T> key = new WidgetThemeKey<>(type, id);
        this.widgetThemeFunctions.put(key, parser);
        this.defaultWidgetThemes.put(key, defaultTheme);
        return key;
    }

    // Internals

    void registerTheme(ITheme theme) {
        if (this.themes.containsKey(theme.getId())) {
            throw new IllegalArgumentException("Theme with id " + theme.getId() + " already exists!");
        }
        this.themes.put(theme.getId(), theme);
    }

    void onReload() {
        this.themes.clear();
        this.jsonScreenThemes.clear();
        registerTheme(DEFAULT_THEME);
    }

    public static class DefaultTheme extends AbstractDefaultTheme {

        private DefaultTheme() {}

        @Override
        public String getId() {
            return DEFAULT_ID;
        }

        @Override
        public <T extends WidgetTheme> T getWidgetTheme(WidgetThemeKey<T> key) {
            if (key.isSubWidgetTheme()) key = Objects.requireNonNull(key.getParent());
            return key.cast(INSTANCE.defaultWidgetThemes.get(key));
        }
    }
}
