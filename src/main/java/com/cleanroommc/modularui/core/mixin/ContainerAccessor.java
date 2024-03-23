package com.cleanroommc.modularui.core.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Container.class)
public interface ContainerAccessor {

    @Accessor
    int getDragEvent();

    @Invoker
    boolean invokeMergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection);

    @Invoker
    void invokeClearContainer(EntityPlayer playerIn, World worldIn, IInventory inventoryIn);
}
