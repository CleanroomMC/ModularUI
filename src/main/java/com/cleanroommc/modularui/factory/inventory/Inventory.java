package com.cleanroommc.modularui.factory.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * A {@link InventoryType} implementation for {@link IInventory}.
 */
public abstract class Inventory extends InventoryType {

    public Inventory(String id) {
        super(id);
    }

    public abstract IInventory getInventory(EntityPlayer player);

    @Override
    public ItemStack getStackInSlot(EntityPlayer player, int index) {
        return getInventory(player).getStackInSlot(index);
    }

    @Override
    public void setStackInSlot(EntityPlayer player, int index, ItemStack stack) {
        getInventory(player).setInventorySlotContents(index, stack);
    }

    @Override
    public int getSlotCount(EntityPlayer player) {
        return getInventory(player).getSizeInventory();
    }
}
