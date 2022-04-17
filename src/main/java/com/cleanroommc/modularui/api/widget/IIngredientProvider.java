package com.cleanroommc.modularui.api.widget;

import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for JEI. Return f.e. a {@link net.minecraft.item.ItemStack} or a {@link net.minecraftforge.fluids.FluidStack} that is stored in this widget.
 */
public interface IIngredientProvider {

    /**
     * @return ingredient stored in this widget
     */
    @Nullable
    Object getIngredient();
}
