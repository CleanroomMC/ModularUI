package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.DrawableSerialization;
import com.cleanroommc.modularui.utils.JsonBuilder;
import com.cleanroommc.modularui.utils.JsonHelper;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class SelectableTheme extends WidgetTheme {

    private final WidgetTheme selected;

    public SelectableTheme(int defaultWidth, int defaultHeight, @Nullable IDrawable background, @Nullable IDrawable hoverBackground,
                           int color, int textColor, boolean textShadow, int iconColor,
                           @Nullable IDrawable selectedBackground, @Nullable IDrawable selectedHoverBackground,
                           int selectedColor, int selectedTextColor, boolean selectedTextShadow, int selectedIconColor) {
        super(defaultWidth, defaultHeight, background, hoverBackground, color, textColor, textShadow, iconColor);
        this.selected = new WidgetTheme(defaultWidth, defaultHeight, selectedBackground, selectedHoverBackground, selectedColor, selectedTextColor, selectedTextShadow, selectedIconColor);
    }

    public SelectableTheme(SelectableTheme parent, JsonObject json, JsonObject fallback) {
        super(parent, json, fallback);
        IDrawable selectedBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parent.getSelected().getBackground(), IThemeApi.SELECTED_BACKGROUND);
        IDrawable selectedHoverBackground = JsonHelper.deserializeWithFallback(json, fallback, IDrawable.class, parent.getSelected().getHoverBackground(), IThemeApi.SELECTED_HOVER_BACKGROUND);
        int selectedColor = JsonHelper.getColorWithFallback(json, fallback, parent.getSelected().getColor(), IThemeApi.SELECTED_COLOR);
        int selectedTextColor = JsonHelper.getColorWithFallback(json, fallback, parent.getSelected().getTextColor(), IThemeApi.SELECTED_TEXT_COLOR);
        boolean selectedTextShadow = JsonHelper.getBoolWithFallback(json, fallback, parent.getSelected().getTextShadow(), IThemeApi.SELECTED_TEXT_SHADOW);
        int selectedIconColor = JsonHelper.getColorWithFallback(json, fallback, parent.getSelected().getTextColor(), IThemeApi.SELECTED_ICON_COLOR);
        this.selected = new WidgetTheme(getDefaultWidth(), getDefaultHeight(), selectedBackground, selectedHoverBackground, selectedColor, selectedTextColor, selectedTextShadow, selectedIconColor);
    }

    public WidgetTheme getSelected() {
        return selected;
    }

    public static class Builder<T extends TextFieldTheme, B extends TextFieldTheme.Builder<T, B>> extends WidgetThemeBuilder<T, B> {

        public B selectedColor(int color) {
            add(IThemeApi.SELECTED_COLOR, color);
            return getThis();
        }

        public B selectedTextColor(int color) {
            add(IThemeApi.SELECTED_TEXT_COLOR, color);
            return getThis();
        }

        public B selectedTextShadow(int shadow) {
            add(IThemeApi.SELECTED_TEXT_SHADOW, shadow);
            return getThis();
        }

        public B selectedIconColor(int color) {
            add(IThemeApi.SELECTED_ICON_COLOR, color);
            return getThis();
        }

        public B selectedBackground(JsonBuilder builder) {
            add(IThemeApi.SELECTED_BACKGROUND, builder);
            return getThis();
        }

        public B selectedBackground(IDrawable drawable) {
            add(IThemeApi.SELECTED_BACKGROUND, DrawableSerialization.serialize(drawable));
            return getThis();
        }

        public B selectedBackground(String textureId) {
            return background(new JsonBuilder().add("type", "texture").add("id", textureId));
        }

        public B selectedHoverBackground(JsonBuilder builder) {
            add(IThemeApi.SELECTED_HOVER_BACKGROUND, builder);
            return getThis();
        }

        public B selectedHoverBackground(IDrawable drawable) {
            add(IThemeApi.SELECTED_HOVER_BACKGROUND, DrawableSerialization.serialize(drawable));
            return getThis();
        }

        public B selectedHoverBackground(String textureId) {
            return background(new JsonBuilder().add("type", "texture").add("id", textureId));
        }
    }
}
