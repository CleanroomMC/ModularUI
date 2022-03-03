package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.widget.DrawableWidget;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface IDrawable {

    IDrawable EMPTY = (pos, size, partialTicks) -> {
    };

    void draw(Pos2d pos, Size size, float partialTicks);

    default void tick() {
    }

    default DrawableWidget asWidget() {
        return new DrawableWidget().setDrawable(this);
    }

    static final Map<String, Function<JsonObject, IDrawable>> JSON_DRAWABLE_MAP = new HashMap<>();

    static IDrawable ofJson(JsonObject json) {
        if (json.has("type")) {
            Function<JsonObject, IDrawable> function = JSON_DRAWABLE_MAP.get(json.get("type").getAsString());
            if (function != null) {
                return function.apply(json);
            }
        }
        return EMPTY;
    }
}
