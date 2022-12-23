package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.keys.CompoundKey;
import com.cleanroommc.modularui.utils.keys.DynamicKey;
import com.cleanroommc.modularui.utils.keys.LangKey;
import com.cleanroommc.modularui.utils.keys.StringKey;

import java.util.function.Supplier;

public interface IKey extends IDrawable {

    TextRenderer renderer = new TextRenderer();

    IKey EMPTY = new StringKey("");

    static IKey lang(String key) {
        return new LangKey(key);
    }

    static IKey format(String key, Object... args) {
        return new LangKey(key).args(args);
    }

    static IKey str(String key) {
        return new StringKey(key);
    }

    static IKey comp(IKey... keys) {
        return new CompoundKey(keys);
    }

    static IKey dynamic(Supplier<String> getter) {
        return new DynamicKey(getter);
    }

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
}