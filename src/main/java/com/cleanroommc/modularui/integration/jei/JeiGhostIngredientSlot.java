package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.Widget;

import mezz.jei.gui.ghost.GhostIngredientDrag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * An interface for compat with JEI's ghost slots.
 * Implement this on any {@link IWidget}.
 * This slot must than be manually registered in something like {@link Widget#onInit()}
 * with {@link com.cleanroommc.modularui.api.JeiSettings#addJeiGhostIngredientSlot(IWidget) JeiSettings.addJeiGhostIngredientSlot(IWidget)}
 *
 * @param <I> type of the ingredient
 */
public interface JeiGhostIngredientSlot<I> {

    /**
     * Puts the ingredient in this ghost slot.
     * Was cast with {@link #castGhostIngredientIfValid(Object)}.
     *
     * @param ingredient ingredient to put
     */
    void setGhostIngredient(@NotNull I ingredient);

    /**
     * Tries to cast an ingredient to the type of this slot.
     * Returns null if the ingredient can't be cast.
     * Must be consistent.
     *
     * @param ingredient ingredient to cast
     * @return cast ingredient or null
     */
    @Nullable
    I castGhostIngredientIfValid(@NotNull Object ingredient);

    default void drawHighlight(Rectangle area, boolean hovering) {
        int color = hovering ? Color.argb(76, 201, 25, 128) : Color.argb(19, 201, 10, 64);
        GuiDraw.drawRect(0, 0, area.width, area.height, color);
    }

    static <T> boolean insertGhostIngredient(GhostIngredientDrag<?> drag, JeiGhostIngredientSlot<T> slot) {
        T t = slot.castGhostIngredientIfValid(drag.getIngredient());
        if (t != null) {
            slot.setGhostIngredient(t);
            return true;
        }
        return false;
    }
}
