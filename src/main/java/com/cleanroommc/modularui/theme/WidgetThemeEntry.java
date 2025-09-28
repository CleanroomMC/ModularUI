package com.cleanroommc.modularui.theme;

public class WidgetThemeEntry<T extends WidgetTheme> {

    private final WidgetThemeKey<T> key;
    private final T theme;
    private final T hoverTheme;

    public WidgetThemeEntry(WidgetThemeKey<T> key, T theme) {
        this(key, theme, theme);
    }

    public WidgetThemeEntry(WidgetThemeKey<T> key, T theme, T hoverTheme) {
        this.key = key;
        this.theme = theme;
        this.hoverTheme = hoverTheme;
    }

    public WidgetThemeKey<T> getKey() {
        return key;
    }

    public T getTheme() {
        return theme;
    }

    public T getHoverTheme() {
        return hoverTheme;
    }

    public T getTheme(boolean hover) {
        return hover ? hoverTheme : theme;
    }

    @SuppressWarnings("unchecked")
    public <F extends WidgetTheme> WidgetThemeEntry<F> expectType(Class<F> expectedType) {
        if (this.key.isOfType(expectedType)) {
            return (WidgetThemeEntry<F>) this;
        }
        throw new IllegalStateException(
                String.format("Got widget theme with invalid type. Got type '%s', but expected type '%s'!",
                        this.key.getWidgetThemeType().getSimpleName(), expectedType.getSimpleName()));
    }
}
