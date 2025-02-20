package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;

/**
 * Wraps a slot and handles interactions for phantom slots.
 * Use {@link ModularSlot} directly.
 */
public class PhantomItemSlotSH extends ItemSlotSH {

    private ItemStack lastStoredPhantomItem = ItemStack.EMPTY;

    @ApiStatus.Internal
    public PhantomItemSlotSH(ModularSlot slot) {
        super(slot);
    }

    @Override
    public void init(String key, PanelSyncManager syncHandler) {
        super.init(key, syncHandler);
        if (isPhantom() && !getSlot().getStack().isEmpty()) {
            this.lastStoredPhantomItem = getSlot().getStack().copy();
            this.lastStoredPhantomItem.setCount(1);
        }
    }

    @Override
    protected void onSlotUpdate(ItemStack stack, boolean onlyAmountChanged, boolean client, boolean init) {
        getSlot().putStack(stack);
        if (!onlyAmountChanged && !stack.isEmpty()) {
            // store last non-empty stack for later
            this.lastStoredPhantomItem = stack.copy();
            this.lastStoredPhantomItem.setCount(1);
        }
        super.onSlotUpdate(stack, onlyAmountChanged, client, init);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        super.readOnServer(id, buf);
        if (id == 2) {
            phantomClick(MouseData.readPacket(buf));
        } else if (id == 3) {
            phantomScroll(MouseData.readPacket(buf));
        } else if (id == 5) {
            if (!isPhantom()) return;
            phantomClick(new MouseData(Side.SERVER, 0, false, false, false), buf.readItemStack());
        }
    }

    protected void phantomClick(MouseData mouseData) {
        phantomClick(mouseData, getSyncManager().getCursorItem());
    }

    protected void phantomClick(MouseData mouseData, ItemStack cursorStack) {
        ItemStack slotStack = getSlot().getStack();
        ItemStack stackToPut;
        if (!cursorStack.isEmpty() && !slotStack.isEmpty() && !ItemHandlerHelper.canItemStacksStack(cursorStack, slotStack)) {
            if (!isItemValid(cursorStack)) return;
            stackToPut = cursorStack.copy();
            if (mouseData.mouseButton == 1) {
                stackToPut.setCount(1);
            }
            stackToPut.setCount(Math.min(stackToPut.getCount(), getSlot().getItemStackLimit(stackToPut)));
            getSlot().putStack(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else if (slotStack.isEmpty()) {
            if (cursorStack.isEmpty()) {
                if (mouseData.mouseButton == 1 && !this.lastStoredPhantomItem.isEmpty()) {
                    stackToPut = this.lastStoredPhantomItem.copy();
                } else {
                    return;
                }
            } else {
                if (!isItemValid(cursorStack)) return;
                stackToPut = cursorStack.copy();
            }
            if (mouseData.mouseButton == 1) {
                stackToPut.setCount(1);
            }
            stackToPut.setCount(Math.min(stackToPut.getCount(), getSlot().getItemStackLimit(stackToPut)));
            getSlot().putStack(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else {
            if (mouseData.mouseButton == 0) {
                if (mouseData.shift) {
                    getSlot().putStack(ItemStack.EMPTY);
                } else {
                    incrementStackCount(-1);
                }
            } else if (mouseData.mouseButton == 1) {
                incrementStackCount(1);
            }
        }
    }

    protected void phantomScroll(MouseData mouseData) {
        ItemStack currentItem = getSlot().getStack();
        int amount = mouseData.mouseButton;
        if (mouseData.shift) amount *= 4;
        if (mouseData.ctrl) amount *= 16;
        if (mouseData.alt) amount *= 64;
        if (amount > 0 && currentItem.isEmpty() && !this.lastStoredPhantomItem.isEmpty()) {
            ItemStack stackToPut = this.lastStoredPhantomItem.copy();
            stackToPut.setCount(amount);
            getSlot().putStack(stackToPut);
        } else {
            incrementStackCount(amount);
        }
    }

    public void incrementStackCount(int amount) {
        ItemStack stack = getSlot().getStack();
        if (stack.isEmpty()) {
            return;
        }
        int oldAmount = stack.getCount();
        if (amount < 0) {
            amount = Math.max(0, oldAmount + amount);
        } else {
            if (Integer.MAX_VALUE - amount < oldAmount) {
                amount = Integer.MAX_VALUE;
            } else {
                int maxSize = getSlot().getSlotStackLimit();
                if (!getSlot().isIgnoreMaxStackSize() && stack.getMaxStackSize() < maxSize) {
                    maxSize = stack.getMaxStackSize();
                }
                amount = Math.min(oldAmount + amount, maxSize);
            }
        }
        if (oldAmount != amount) {
            stack = stack.copy();
            stack.setCount(amount);
            getSlot().putStack(stack);
        }
    }

    @Override
    public boolean isPhantom() {
        return true;
    }
}
