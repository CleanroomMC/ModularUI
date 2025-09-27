package com.cleanroommc.modularui.theme;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class WidgetThemeMap extends Object2ObjectOpenHashMap<WidgetThemeKey<?>, WidgetThemeEntry<?>> {

    @Override
    public WidgetThemeEntry<?> put(WidgetThemeKey<?> widgetThemeKey, WidgetThemeEntry<?> widgetTheme) {
        if (widgetThemeKey != widgetTheme.getKey()) {
            throw new IllegalArgumentException(widgetThemeKey.getFullName() + " is not compatible with " + widgetTheme.getClass().getSimpleName());
        }
        return super.put(widgetThemeKey, widgetTheme);
    }

    @SuppressWarnings("unchecked")
    public <T extends WidgetTheme> WidgetThemeEntry<T> putTheme(WidgetThemeKey<T> widgetThemeKey, WidgetThemeEntry<T> widgetTheme) {
        return (WidgetThemeEntry<T>) super.put(widgetThemeKey, widgetTheme);
    }

    @SuppressWarnings("unchecked")
    public <T extends WidgetTheme> WidgetThemeEntry<T> putTheme(WidgetThemeKey<T> widgetThemeKey, T widgetTheme) {
        return putTheme(widgetThemeKey, widgetTheme, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends WidgetTheme> WidgetThemeEntry<T> putTheme(WidgetThemeKey<T> widgetThemeKey, T widgetTheme, boolean hover) {
        if (hover) {
            WidgetThemeEntry<T> entry = getTheme(widgetThemeKey);
            if (entry != null) {
                entry = entry.withHoverTheme(widgetTheme);
                return putTheme(widgetThemeKey, entry);
            }
            throw new NullPointerException();
        }
        return (WidgetThemeEntry<T>) super.put(widgetThemeKey, new WidgetThemeEntry<>(widgetThemeKey, widgetTheme));
    }

    @SuppressWarnings("unchecked")
    public <T extends WidgetTheme> WidgetThemeEntry<T> getTheme(WidgetThemeKey<T> widgetThemeKey) {
        return (WidgetThemeEntry<T>) super.get(widgetThemeKey);
    }
}
