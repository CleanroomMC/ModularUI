package com.cleanroommc.modularui.api.widget;

import mezz.jei.api.gui.IGhostIngredientHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for {@link Widget} classes.
 * Implement this, to be able to drag items from JEI onto this widget
 *
 * @param <I> Type of Object the widget accepts
 */
public interface IGhostIngredientTarget<I> {

    /**
     * Called when the users tries to drag an object from JEI.
     *
     * @param ingredient object the cursor is holding. Should be validated for Type
     * @return the JEI target. Usually an instance of {@link com.cleanroommc.modularui.common.internal.wrapper.GhostIngredientWrapper}
     */
    @Nullable
    IGhostIngredientHandler.Target<I> getTarget(@NotNull Object ingredient);

    /**
     * Called when this widget is clicked with a object
     *
     * @param ingredient object the cursor is holding
     */
    void accept(@NotNull I ingredient);
}
