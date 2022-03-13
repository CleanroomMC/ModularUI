package com.cleanroommc.modularui.common.internal.wrapper;

import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.ModularWindow;
import com.cleanroommc.modularui.integration.vanilla.slot.BaseSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemHandlerHelper;

public class ModularUIContainer extends Container {

    private final ModularUIContext context;

    public ModularUIContainer(ModularUIContext context, ModularWindow mainWindow) {
        this.context = context;
        this.context.initialize(this, mainWindow);
        sortSlots();
    }

    public void sortSlots() {
        this.inventorySlots.sort((slot, slot1) -> {
            if (slot instanceof BaseSlot) {
                if (slot1 instanceof BaseSlot) {
                    return Integer.compare(((BaseSlot) slot).getShiftClickPriority(), ((BaseSlot) slot1).getShiftClickPriority());
                }
                return 1;
            } else if (slot1 instanceof BaseSlot) {
                return -1;
            }
            return 0;
        });
        this.inventoryItemStacks = NonNullList.create();
        for (int i = 0; i < this.inventorySlots.size(); i++) {
            Slot slot = this.inventorySlots.get(i);
            slot.slotNumber = i;
            this.inventoryItemStacks.add(ItemStack.EMPTY);
        }
    }

    public ModularUIContext getContext() {
        return context;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.isEntityAlive();
    }

    @Override
    public Slot addSlotToContainer(Slot slotIn) {
        return super.addSlotToContainer(slotIn);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (context.isInitialized()) {
            // do not allow syncing before the client is initialized
            context.getCurrentWindow().serverUpdate();
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot slot = this.inventorySlots.get(index);
        if (slot instanceof BaseSlot) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                ItemStack remainder = transferItem((BaseSlot) slot, stack.copy());
                stack.setCount(remainder.getCount());
                return remainder;
            }
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack transferItem(BaseSlot fromSlot, ItemStack stack) {
        for (Slot slot1 : this.inventorySlots) {
            if (!(slot1 instanceof BaseSlot)) {
                continue;
            }
            BaseSlot slot = (BaseSlot) slot1;
            if (fromSlot.getShiftClickPriority() != slot.getShiftClickPriority() && slot.isEnabled() && slot.isItemValid(stack)) {
                ItemStack itemstack = slot.getStack();
                if (ItemHandlerHelper.canItemStacksStackRelaxed(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.onSlotChanged();
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.onSlotChanged();
                    }

                    if (stack.isEmpty()) {
                        return stack;
                    }
                }
            }
        }
        for (Slot slot1 : this.inventorySlots) {
            if (!(slot1 instanceof BaseSlot)) {
                continue;
            }
            BaseSlot slot = (BaseSlot) slot1;
            ItemStack itemstack = slot.getStack();
            if (fromSlot.getItemHandler() != slot.getItemHandler() && slot.isEnabled() && itemstack.isEmpty() && slot.isItemValid(stack)) {
                if (stack.getCount() > slot1.getSlotStackLimit()) {
                    slot.putStack(stack.splitStack(slot.getSlotStackLimit()));
                } else {
                    slot.putStack(stack.splitStack(stack.getCount()));
                }
                break;
            }
        }
        return stack;
    }
}
