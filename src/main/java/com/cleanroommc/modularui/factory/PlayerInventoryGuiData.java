package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.factory.inventory.InventoryType;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PlayerInventoryGuiData extends GuiData {

    private final InventoryType inventoryType;
    private final int slotIndex;

    public PlayerInventoryGuiData(EntityPlayer player, InventoryType inventoryType, int slotIndex) {
        super(player);
        this.inventoryType = inventoryType;
        this.slotIndex = slotIndex;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public ItemStack getUsedItemStack() {
        return getInventoryType().getStackInSlot(getPlayer(), this.slotIndex);
    }

    public void setUsedItemStack(ItemStack stack) {
        getInventoryType().setStackInSlot(getPlayer(), this.slotIndex, stack);
    }
}
