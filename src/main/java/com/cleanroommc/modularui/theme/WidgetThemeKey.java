package com.cleanroommc.modularui.theme;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * A key used to identify widget themes.
 * @param <T> type of associated widget theme
 */
public class WidgetThemeKey<T extends WidgetTheme> {

    private static final Map<String, WidgetThemeKey<?>> KEYS = new Object2ObjectOpenHashMap<>();

    public static @Nullable WidgetThemeKey<?> getFromFullName(String key) {
        return KEYS.get(key);
    }

    @Nullable private final WidgetThemeKey<T> parent;
    private final Class<T> type;
    private final String name;
    @Nullable private final String subName;

    WidgetThemeKey(Class<T> type, String name) {
        this(null, type, name, null);
    }

    private WidgetThemeKey(@Nullable WidgetThemeKey<T> parent, Class<T> type, String name, @Nullable String subName) {
        this.parent = parent;
        this.type = type;
        this.name = name;
        this.subName = subName;
        KEYS.put(getFullName(), this);
    }

    public WidgetThemeKey<T> createSubKey(String subName) {
        WidgetThemeKey<?> existing = KEYS.get(getName() + ":" + subName);
        if (existing != null) {
            if (existing.type == type) {
                return (WidgetThemeKey<T>) existing;
            }
            throw new IllegalStateException("A widget theme key for id " + getName() + ":" + subName + " already exists, but with a different type.");
        }
        WidgetThemeKey<T> parent = this;
        if (parent.parent != null) {
            parent = parent.parent;
        }
        return new WidgetThemeKey<>(parent, type, name, subName);
    }

    public @Nullable WidgetThemeKey<T> getParent() {
        return parent;
    }

    public Class<T> getWidgetThemeType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getSubName() {
        return subName;
    }

    public String getFullName() {
        if (subName != null) {
            return name + ":" + subName;
        }
        return name;
    }

    public boolean isSubWidgetTheme() {
        return parent != null;
    }

    public boolean isCompatible(WidgetTheme theme) {
        return type.isInstance(theme);
    }

    public boolean isExactType(WidgetTheme theme) {
        return theme != null && type == theme.getClass();
    }

    public T cast(WidgetTheme theme) {
        return type.cast(theme);
    }

    public T getDefault() {
        return cast(ThemeAPI.INSTANCE.defaultWidgetThemes.get(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WidgetThemeKey<?> that = (WidgetThemeKey<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(subName, that.subName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, subName);
    }
}
