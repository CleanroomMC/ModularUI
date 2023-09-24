package com.cleanroommc.bogosorter.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * A custom slot interface. Useful if mods have a slot that does not implement the necessary methods.
 * {@link Slot} implements this interface via mixin.
 */
public interface ISlot {

    Slot bogo$getRealSlot();

    int bogo$getX();

    int bogo$getY();

    int bogo$getSlotNumber();

    int bogo$getSlotIndex();

    IInventory bogo$getInventory();

    void bogo$putStack(ItemStack itemStack);

    ItemStack bogo$getStack();

    int bogo$getMaxStackSize(ItemStack itemStack);

    int bogo$getItemStackLimit(ItemStack itemStack);

    boolean bogo$isEnabled();

    boolean bogo$isItemValid(ItemStack stack);

    boolean bogo$canTakeStack(EntityPlayer player);

    void bogo$onSlotChanged();
}
