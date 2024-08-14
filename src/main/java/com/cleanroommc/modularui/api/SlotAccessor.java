package com.cleanroommc.modularui.api;

import net.minecraft.world.item.ItemStack;

/**
 * Accessor interface for accessing protected methods from {@link net.minecraft.world.inventory.Slot}.
 */
public interface SlotAccessor {

    void invokeOnCrafting(ItemStack stack, int amount);

    void invokeOnSwapCraft(int p_190900_1_);

    void invokeOnCrafting(ItemStack stack);
}
