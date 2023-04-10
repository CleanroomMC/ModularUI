package com.cleanroommc.modularui.integration.jei;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import org.jetbrains.annotations.ApiStatus;

/**
 * An interface to handle recipe transfers.
 * Implement this on {@link com.cleanroommc.modularui.screen.ModularScreen}.
 * No further registration needed.
 */
@ApiStatus.Experimental
public interface JeiRecipeTransferHandler {

    /**
     * Transfers a JEI recipe.
     *
     * @param recipeLayout recipe layout
     * @param maxTransfer  true if shift is being held
     * @param simulate     if the transfer is simulated
     * @return a transfer error or null if successful
     */
    IRecipeTransferError transferRecipe(IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate);
}
