package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.core.mixin.InventoryCraftingAccessor;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class InventoryCraftingWrapper extends InventoryCrafting {

    private final IItemHandler delegate;
    private final int startIndex;
    private final ItemStack[] snapshot;

    public InventoryCraftingWrapper(Container eventHandlerIn, int width, int height, IItemHandlerModifiable delegate, int startIndex) {
        super(eventHandlerIn, width, height);
        this.delegate = delegate;
        this.startIndex = startIndex;
        this.snapshot = new ItemStack[width * height];
        // save inventory snapshot
        for (int i = 0; i < snapshot.length; i++) {
            ItemStack stack = this.delegate.getStackInSlot(i + this.startIndex);
            updateSnapshot(i, stack);
            getBackingList().set(i, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
    }

    private NonNullList<ItemStack> getBackingList() {
        return ((InventoryCraftingAccessor) this).getStackList();
    }

    public Container getEventHandler() {
        return ((InventoryCraftingAccessor) this).getEventHandler();
    }

    private void updateSnapshot(int index, ItemStack stack) {
        this.snapshot[index] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    public void detectChanges() {
        // detect changes from snapshot and notify container
        for (int i = 0; i < snapshot.length; i++) {
            ItemStack stack = snapshot[i];
            ItemStack current = this.delegate.getStackInSlot(i + this.startIndex);
            if (stack.isEmpty() != current.isEmpty() || (!stack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(stack, current))) {
                setInventorySlotContents(i, current);
                updateSnapshot(i, current);
            }
        }
    }

    public IItemHandler getDelegate() {
        return delegate;
    }

    public int getStartIndex() {
        return startIndex;
    }
}
