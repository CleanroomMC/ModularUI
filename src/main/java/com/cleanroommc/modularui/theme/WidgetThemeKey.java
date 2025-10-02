package com.cleanroommc.modularui.theme;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * A key used to identify widget themes.
 * @param <T> type of associated widget theme
 */
public class WidgetThemeKey<T extends WidgetTheme> implements Comparable<WidgetThemeKey<?>> {

    private static final Map<String, WidgetThemeKey<?>> KEYS = new Object2ObjectOpenHashMap<>();

    public static @Nullable WidgetThemeKey<?> getFromFullName(String key) {
        return KEYS.get(key);
    }

    @Nullable private final WidgetThemeKey<T> parent;
    private final Class<T> type;
    private final String name;
    @Nullable private final String subName;
    private final T defaultValue;
    private final T defaultHoverValue;
    private final WidgetThemeParser<T> parser;

    WidgetThemeKey(Class<T> type, String name, T defaultValue, WidgetThemeParser<T> parser) {
        this(type, name, defaultValue, defaultValue, parser);
    }

    WidgetThemeKey(Class<T> type, String name, T defaultValue, T defaultHoverValue, WidgetThemeParser<T> parser) {
        this(null, type, name, null, defaultValue, defaultHoverValue, parser);
    }

    private WidgetThemeKey(@Nullable WidgetThemeKey<T> parent, Class<T> type, String name, @Nullable String subName, T defaultValue, T defaultHoverValue, WidgetThemeParser<T> parser) {
        this.parent = parent;
        this.type = type;
        this.name = name;
        this.subName = subName;
        this.defaultValue = defaultValue;
        this.defaultHoverValue = defaultHoverValue;
        this.parser = parser;
        KEYS.put(getFullName(), this);
        ThemeAPI.INSTANCE.registerWidgetThemeKey(this);
    }

    public WidgetThemeKey<T> createSubKey(String subName) {
        return createSubKey(subName, null, null);
    }

    public WidgetThemeKey<T> createSubKey(String subName, @Nullable T defaultValue, @Nullable T defaultHoverValue) {
        WidgetThemeKey<?> existing = KEYS.get(getName() + ":" + subName);
        if (existing != null) {
            if (existing.type == type) {
                return (WidgetThemeKey<T>) existing;
            }
            throw new IllegalStateException("A widget theme key for id " + getName() + ":" + subName +
                    " already exists, but with a different types '" + existing.type.getSimpleName() + "' and '" + type.getSimpleName() + "'.");
        }
        return new WidgetThemeKey<>(this, type, name, subName,
                defaultValue != null ? defaultValue : getDefaultValue(),
                defaultHoverValue != null ? defaultHoverValue : getDefaultValue(),
                this.parser);
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

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getDefaultHoverValue() {
        return defaultHoverValue;
    }

    public WidgetThemeParser<T> getParser() {
        return parser;
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

    public boolean isOfType(Class<? extends WidgetTheme> type) {
        return type.isAssignableFrom(this.type);
    }

    public T cast(WidgetTheme theme) {
        return type.cast(theme);
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

    @Override
    public int compareTo(@NotNull WidgetThemeKey<?> o) {
        if (o == this) return 0;
        int i = Boolean.compare(isSubWidgetTheme(), o.isSubWidgetTheme());
        if (i != 0) return i;
        i = isAncestor(o);
        if (i != 0) return i;
        return name.compareTo(o.name);
    }

    private int isAncestor(WidgetThemeKey<?> other) {
        WidgetThemeKey<?> parent = getParent();
        while (parent != null) {
            if (parent == other) return 1;
            parent = parent.getParent();
        }
        parent = other.getParent();
        while (parent != null) {
            if (parent == this) return -1;
            parent = parent.getParent();
        }
        return 0;
    }
}
