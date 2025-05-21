package com.cleanroommc.modularui.factory.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * A {@link InventoryType} implementation for {@link IItemHandlerModifiable}.
 */
public abstract class ItemHandler extends InventoryType {

    public ItemHandler(String id) {
        super(id);
    }

    public abstract IItemHandlerModifiable getInventory(EntityPlayer player);

    @Override
    public ItemStack getStackInSlot(EntityPlayer player, int index) {
        return getInventory(player).getStackInSlot(index);
    }

    @Override
    public void setStackInSlot(EntityPlayer player, int index, ItemStack stack) {
        getInventory(player).setStackInSlot(index, stack);
    }

    @Override
    public int getSlotCount(EntityPlayer player) {
        return getInventory(player).getSlots();
    }
}
