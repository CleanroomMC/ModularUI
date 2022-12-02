package com.cleanroommc.modularui.core.mixin;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Slot.class)
public interface SlotAccessor {

    @Invoker
    void invokeOnCrafting(ItemStack stack, int amount);

    @Invoker
    void invokeOnSwapCraft(int p_190900_1_);

    @Invoker
    void invokeOnCrafting(ItemStack stack);

    @Invoker
    TextureMap invokeGetBackgroundMap();
}
