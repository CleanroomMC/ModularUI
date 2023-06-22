package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.api.future.IItemHandler;
import com.cleanroommc.modularui.api.future.SlotItemHandler;
import com.cleanroommc.modularui.sync.ItemSlotSH;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class SlotCustomSlot extends SlotItemHandler implements ICustomSlot {

    private ItemSlotSH syncHandler;
    private int slotLimit = 64;
    private Predicate<ItemStack> filter;
    private boolean ignoreMaxStackSize = false;
    private boolean enabled = true;

    public SlotCustomSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    public void setSyncHandler(ItemSlotSH syncHandler) {
        this.syncHandler = syncHandler;
    }

    // @Override // nh todo
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void onSlotChange(@NotNull ItemStack p_75220_1_, @NotNull ItemStack p_75220_2_) {

    }

    protected void onCrafting(@NotNull ItemStack stack, int amount) {
    }

    protected void onSwapCraft(int p_190900_1_) {
    }

    protected void onCrafting(@NotNull ItemStack stack) {
    }

    public @NotNull ItemStack onTake(@NotNull EntityPlayer thePlayer, @NotNull ItemStack stack) {
        this.onSlotChanged();
        return stack;
    }

    public boolean isItemValid(@NotNull ItemStack stack) {
        if (syncHandler != null && syncHandler.isPhantom()) return false;
        return (this.filter == null || this.filter.test(stack)) && super.isItemValid(stack);
    }

    public boolean canTakeStack(EntityPlayer playerIn) {
        return true;
    }


    public boolean isSameInventory(Slot other) {
        return this.inventory == other.inventory;
    }

    public boolean isIgnoreMaxStackSize() {
        return ignoreMaxStackSize;
    }

    public Predicate<ItemStack> getFilter() {
        return filter;
    }

    public void setIgnoreMaxStackSize(boolean ignoreMaxStackSize) {
        this.ignoreMaxStackSize = ignoreMaxStackSize;
    }
}
