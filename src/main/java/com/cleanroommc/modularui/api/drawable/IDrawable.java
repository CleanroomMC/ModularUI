package com.cleanroommc.modularui.api.drawable;

import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.cleanroommc.modularui.common.widget.DrawableWidget;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    @SideOnly(Side.CLIENT)
    void draw(float x, float y, float width, float height, float partialTicks);

    @SideOnly(Side.CLIENT)
    default void draw(Pos2d pos, Size size, float partialTicks) {
        draw(pos.x, pos.y, size.width, size.height, partialTicks);
    }

    default void tick() {
    }

    @SideOnly(Side.CLIENT)
    default void applyThemeColor(int color) {
        GlStateManager.color(Color.getRedF(color), Color.getGreenF(color), Color.getBlueF(color), Color.getAlphaF(color));
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
        IDrawable drawable = EMPTY;
        if (json.has("type")) {
            Function<JsonObject, IDrawable> function = JSON_DRAWABLE_MAP.get(json.get("type").getAsString());
            if (function != null) {
                drawable = function.apply(json);
            }
        }
        Pos2d offset = JsonHelper.getElement(json, Pos2d.ZERO, Pos2d::ofJson, "offset");
        Size offsetSize = JsonHelper.getElement(json, Size.ZERO, Size::ofJson, "offsetSize");
        Size fixedSize = JsonHelper.getElement(json, Size.ZERO, Size::ofJson, "fixedSize");
        if (!fixedSize.isZero()) {
            return drawable.withFixedSize(fixedSize.width, fixedSize.height, offset.x, offset.y);
        }
        if (!offset.isZero() || !offsetSize.isZero()) {
            return drawable.withOffset(offset.x, offset.y, offsetSize.width, offsetSize.height);
        }

        return drawable;
    }
}
