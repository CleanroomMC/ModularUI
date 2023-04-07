package com.cleanroommc.modularui.mixins;

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
    protected abstract void onCrafting(ItemStack stack);

    @Override
    public void invokeOnCrafting(ItemStack stack, int amount) {
        onCrafting(stack, amount);
    }

    @Override
    public void invokeOnCrafting(ItemStack stack) {
        onCrafting(stack);
    }
}
