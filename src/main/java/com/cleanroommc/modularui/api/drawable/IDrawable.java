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

    /**
     * Empty drawable
     */
    IDrawable EMPTY = (x, y, width, height, partialTicks) -> {
    };

    /**
     * Called ever frame
     *
     * @param x            x position
     * @param y            y position
     * @param width        width of the drawable
     * @param height       height of the drawable
     * @param partialTicks ticks since last render
     */
    void draw(float x, float y, float width, float height, float partialTicks);

    default void draw(Pos2d pos, Size size, float partialTicks) {
        draw(pos.x, pos.y, size.width, size.height, partialTicks);
    }

    default void tick() {
    }

    /**
     * @return a drawable that can be used in guis as a widget
     */
    default DrawableWidget asWidget() {
        return new DrawableWidget().setDrawable(this);
    }

    /**
     * This drawable with an offset pos.
     * Useful if the background of a widget should be larger than the widget itself.
     *
     * @param offsetX      offset in x
     * @param offsetY      offset in y
     * @param widthOffset  offset width (added to the width passed in {@link #draw(float, float, float, float, float)})
     * @param heightOffset offset height (added to the height passed in {@link #draw(float, float, float, float, float)})
     * @return this drawable with offset
     */
    default IDrawable withOffset(float offsetX, float offsetY, float widthOffset, float heightOffset) {
        return new OffsetDrawable(this, offsetX, offsetY, widthOffset, heightOffset);
    }

    default IDrawable withOffset(float offsetX, float offsetY) {
        return new OffsetDrawable(this, offsetX, offsetY);
    }

    /**
     * This drawable with a fixed size.
     *
     * @param fixedHeight fixed width (ignores width passed in {@link #draw(float, float, float, float, float)})
     * @param fixedWidth  fixed height (ignores height passed in {@link #draw(float, float, float, float, float)})
     * @param offsetX     offset in x
     * @param offsetY     offset in y
     * @return this drawable with offset
     */
    default IDrawable withFixedSize(float fixedWidth, float fixedHeight, float offsetX, float offsetY) {
        return new SizedDrawable(this, fixedWidth, fixedHeight, offsetX, offsetY);
    }

    default IDrawable withFixedSize(float fixedWidth, float fixedHeight) {
        return new SizedDrawable(this, fixedWidth, fixedHeight);
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
