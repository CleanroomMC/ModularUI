package com.cleanroommc.modularui.core.mixins.early.minecraft;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InventoryCrafting.class)
public interface InventoryCraftingAccessor {

    @Accessor
    NonNullList<ItemStack> getStackList();

    @Accessor
    Container getEventHandler();
}
