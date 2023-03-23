package com.cleanroommc.modularui.core.mixin;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Slot.class)
public interface SlotAccessor {

    @Invoker(remap = false)
    void invokeOnCrafting(ItemStack stack, int amount);

    @Invoker
    void invokeOnSwapCraft(int p_190900_1_);

    @Invoker("onCrafting")
    void invokeOnCrafting(ItemStack stack);
}
