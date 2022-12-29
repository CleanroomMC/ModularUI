package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.keys.CompoundKey;
import com.cleanroommc.modularui.utils.keys.DynamicKey;
import com.cleanroommc.modularui.utils.keys.LangKey;
import com.cleanroommc.modularui.utils.keys.StringKey;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.TextWidget;

import java.util.function.Supplier;

/**
 * This represents a piece of text in a GUI.
 */
public interface IKey extends IDrawable {

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

    @Override
    default void draw(int x, int y, int width, int height) {
        renderer.setAlignment(Alignment.Center, width, height);
        renderer.setColor(0);
        renderer.setPos(x, y);
        renderer.setShadow(false);
        renderer.draw(get());
    }

    @Override
    default Widget<?> asWidget() {
        return new TextWidget(this);
    }
}