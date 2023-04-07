package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.api.SlotAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SlotDelegate extends Slot implements ICustomSlot {

    private boolean enabled = true;

    public static SlotDelegate create(Slot slot) {
        return create(slot, false);
    }

    public static SlotDelegate create(Slot slot, boolean force) {
        Objects.requireNonNull(slot);
        if (force) {
            return new SlotDelegate(slot);
        }
        Slot delegated = slot;
        while (delegated instanceof SlotDelegate) {
            delegated = ((SlotDelegate) delegated).getDelegate();
        }
        SlotDelegate newDelegate = new SlotDelegate(delegated);
        newDelegate.xDisplayPosition = slot.xDisplayPosition;
        newDelegate.yDisplayPosition = slot.yDisplayPosition;
        return newDelegate;
    }

    private final Slot slot;

    private SlotDelegate(Slot slot) {
        super(slot.inventory, slot.slotNumber, slot.xDisplayPosition, slot.yDisplayPosition);
        this.slot = slot;
    }

    @Override
    public void onSlotChange(@NotNull ItemStack p_75220_1_, @NotNull ItemStack p_75220_2_) {
        slot.onSlotChange(p_75220_1_, p_75220_2_);
    }

    @Override
    protected void onCrafting(@NotNull ItemStack stack, int amount) {
        ((SlotAccessor) slot).invokeOnCrafting(stack, amount);
    }

    @Override
    protected void onCrafting(@NotNull ItemStack stack) {
        ((SlotAccessor) slot).invokeOnCrafting(stack);
    }

    @Override
    public void onPickupFromSlot(@NotNull EntityPlayer thePlayer, @NotNull ItemStack stack) {
        slot.onPickupFromSlot(thePlayer, stack);
    }

    @Override
    public boolean isItemValid(@NotNull ItemStack stack) {
        return slot.isItemValid(stack);
    }

    @Override
    @NotNull
    public ItemStack getStack() {
        return slot.getStack();
    }

    @Override
    public boolean getHasStack() {
        return slot.getHasStack();
    }

    @Override
    public void putStack(@NotNull ItemStack stack) {
        slot.putStack(stack);
    }

    @Override
    public void onSlotChanged() {
        slot.onSlotChanged();
    }

    @Override
    public int getSlotStackLimit() {
        return slot.getSlotStackLimit();
    }

    @Override
    @NotNull
    public ItemStack decrStackSize(int amount) {
        return slot.decrStackSize(amount);
    }

    @Override
    public boolean isSlotInInventory(@NotNull IInventory inv, int slotIn) {
        return slot.isSlotInInventory(inv, slotIn);
    }

    @Override
    public boolean canTakeStack(@NotNull EntityPlayer playerIn) {
        return slot.canTakeStack(playerIn);
    }

    @Override
    public IIcon getBackgroundIconIndex() {
        return slot.getBackgroundIconIndex();
    }

    @Override
    public boolean func_111238_b() {
        return this.enabled && slot.func_111238_b();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    @NotNull
    public ResourceLocation getBackgroundIconTexture() {
        return slot.getBackgroundIconTexture();
    }

    @Override
    public void setBackgroundIcon(IIcon icon) {
        slot.setBackgroundIcon(icon);
    }

    @Override
    public void setBackgroundIconTexture(ResourceLocation texture) {
        slot.setBackgroundIconTexture(texture);
    }

    @Override
    public int getSlotIndex() {
        return slot.getSlotIndex();
    }

    public Slot getDelegate() {
        return slot;
    }
}
