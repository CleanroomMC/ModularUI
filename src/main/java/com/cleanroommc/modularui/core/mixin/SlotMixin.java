package com.cleanroommc.modularui.core.mixin;

import com.cleanroommc.modularui.api.SlotAccessor;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Slot.class)
public abstract class SlotMixin implements SlotAccessor {

    @Shadow
    protected abstract void onCrafting(ItemStack stack, int amount);

    @Shadow
    protected abstract void onSwapCraft(int p_190900_1_);

    @Shadow
    protected abstract void onCrafting(ItemStack stack);

    @Override
    public void invokeOnCrafting(ItemStack stack, int amount) {
        onCrafting(stack, amount);
    }

    @Override
    public void invokeOnSwapCraft(int p_190900_1_) {
        onSwapCraft(p_190900_1_);
    }

    @Override
    public void invokeOnCrafting(ItemStack stack) {
        onCrafting(stack);
    }
}
