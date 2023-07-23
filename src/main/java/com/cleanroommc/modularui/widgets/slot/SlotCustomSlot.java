package com.cleanroommc.modularui.widgets.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class SlotCustomSlot extends SlotItemHandler implements ICustomSlot {

    private final int slotLimit = 64;
    private Predicate<ItemStack> filter;
    private boolean ignoreMaxStackSize = false;
    private boolean enabled = true;

    public SlotCustomSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void onSlotChange(@NotNull ItemStack p_75220_1_, @NotNull ItemStack p_75220_2_) {

    }

    protected void onCrafting(@NotNull ItemStack stack, int amount) {
    }

    protected void onCrafting(@NotNull ItemStack stack) {
    }

    public @NotNull ItemStack onTake(@NotNull EntityPlayer thePlayer, @NotNull ItemStack stack) {
        this.onSlotChanged();
        return stack;
    }

    public boolean isItemValid(@NotNull ItemStack stack) {
        return (this.filter == null || this.filter.test(stack)) && super.isItemValid(stack);
    }

    public boolean canTakeStack(EntityPlayer playerIn) {
        return true;
    }


    public boolean isSameInventory(Slot other) {
        return this.inventory == other.inventory;
    }

    public boolean isIgnoreMaxStackSize() {
        return this.ignoreMaxStackSize;
    }

    public Predicate<ItemStack> getFilter() {
        return this.filter;
    }

    public void setIgnoreMaxStackSize(boolean ignoreMaxStackSize) {
        this.ignoreMaxStackSize = ignoreMaxStackSize;
    }
}
