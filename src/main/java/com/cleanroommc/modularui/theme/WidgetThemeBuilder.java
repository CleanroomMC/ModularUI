package com.cleanroommc.modularui.theme;

import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.DrawableSerialization;
import com.cleanroommc.modularui.utils.JsonBuilder;

/**
 * A json builder with helper methods to make building widget themes in java easier.
 * This class is meant to be extended for custom widget themes.
 *
 * @param <T> type of the widget theme this builder is for
 * @param <B> type of this builder class
 */
public class WidgetThemeBuilder<T extends WidgetTheme, B extends WidgetThemeBuilder<T, B>> extends JsonBuilder {

    @SuppressWarnings("unchecked")
    protected B getThis() {
        return (B) this;
    }

    public B defaultWidth(int defaultWidth) {
        add(IThemeApi.DEFAULT_WIDTH, defaultWidth);
        return getThis();
    }

    public B defaultHeight(int defaultHeight) {
        add(IThemeApi.DEFAULT_HEIGHT, defaultHeight);
        return getThis();
    }

    public B color(int color) {
        add(IThemeApi.COLOR, color);
        return getThis();
    }

    public B textColor(int color) {
        add(IThemeApi.TEXT_COLOR, color);
        return getThis();
    }

    public B textShadow(int shadow) {
        add(IThemeApi.TEXT_SHADOW, shadow);
        return getThis();
    }

    public B iconColor(int color) {
        add(IThemeApi.ICON_COLOR, color);
        return getThis();
    }

    public B background(JsonBuilder builder) {
        add(IThemeApi.BACKGROUND, builder);
        return getThis();
    }

    public B background(IDrawable drawable) {
        add(IThemeApi.BACKGROUND, DrawableSerialization.serialize(drawable));
        return getThis();
    }

    public B background(String textureId) {
        return background(new JsonBuilder().add("type", "texture").add("id", textureId));
    }
}
