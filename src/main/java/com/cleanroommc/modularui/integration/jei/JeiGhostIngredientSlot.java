package com.cleanroommc.modularui.integration.jei;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for compat with JEI's ghost slots.
 * Implement this on any {@link IWidget}.
 * This slot must than be manually registered in something like {@link Widget#onInit()}
 * with {@link GuiContext#addJeiGhostIngredientSlot(IWidget)}
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
}
