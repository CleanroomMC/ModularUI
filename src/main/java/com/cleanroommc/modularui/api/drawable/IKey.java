package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.drawable.AnimatedText;
import com.cleanroommc.modularui.drawable.StyledText;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.drawable.keys.CompoundKey;
import com.cleanroommc.modularui.drawable.keys.DynamicKey;
import com.cleanroommc.modularui.drawable.keys.LangKey;
import com.cleanroommc.modularui.drawable.keys.StringKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.JsonHelper;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Supplier;

/**
 * This represents a piece of text in a GUI.
 */
public interface IKey extends IDrawable {

    int TEXT_COLOR = 0xFF404040;

    TextRenderer renderer = new TextRenderer();

    IKey EMPTY = new StringKey("");

    /**
     * Creates a translated text.
     *
     * @param key translation key
     * @return text key
     */
    static IKey lang(String key) {
        return new LangKey(key);
    }

    /**
     * Creates a translated text with arguments. The arguments can change.
     *
     * @param key  translation key
     * @param args translation arguments
     * @return text key
     */
    static IKey format(String key, Object... args) {
        return new LangKey(key).args(args);
    }

    /**
     * Creates a string literal text.
     *
     * @param key string
     * @return text key
     */
    static IKey str(String key) {
        return new StringKey(key);
    }

    /**
     * Creates a composed text key.
     *
     * @param keys text keys
     * @return composed text key.
     */
    static IKey comp(IKey... keys) {
        return new CompoundKey(keys);
    }

    /**
     * Creates a dynamic text key.
     *
     * @param getter string supplier
     * @return dynamic text key
     */
    static IKey dynamic(Supplier<String> getter) {
        return new DynamicKey(getter);
    }

    /**
     * @return the current formatted string
     */
    String get();

    @Deprecated
    void set(String string);

    @SideOnly(Side.CLIENT)
    @Override
    default void draw(GuiContext context, int x, int y, int width, int height) {
        renderer.setAlignment(Alignment.Center, width, height);
        renderer.setScale(1f);
        renderer.setPos(x, y);
        renderer.draw(get());
    }

    @Override
    default void applyThemeColor(ITheme theme, WidgetTheme widgetTheme) {
        renderer.setColor(widgetTheme.getTextColor());
        renderer.setShadow(widgetTheme.getTextShadow());
    }

    @Override
    default TextWidget asWidget() {
        return new TextWidget(this);
    }

    default StyledText withStyle() {
        return new StyledText(this);
    }

    default AnimatedText withAnimation() {
        return new AnimatedText(this);
    }

    default StyledText alignment(Alignment alignment) {
        return withStyle().alignment(alignment);
    }

    default StyledText color(int color) {
        return withStyle().color(color);
    }

    default StyledText scale(float scale) {
        return withStyle().scale(scale);
    }

    default StyledText shadow(boolean shadow) {
        return withStyle().shadow(shadow);
    }

    @Override
    default void loadFromJson(JsonObject json) {
        if (json.has("color") || json.has("shadow") || json.has("align") || json.has("alignment") || json.has("scale")) {
            StyledText styledText = this instanceof StyledText ? (StyledText) this : withStyle();
            if (json.has("color")) {
                styledText.color(JsonHelper.getInt(json, 0, "color"));
            }
            styledText.shadow(JsonHelper.getBoolean(json, false, "shadow"));
            styledText.alignment(JsonHelper.deserialize(json, Alignment.class, styledText.getAlignment(), "align", "alignment"));
            styledText.scale(JsonHelper.getFloat(json, 1, "scale"));
        }
    }
}