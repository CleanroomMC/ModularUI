package com.cleanroommc.modularui.value.sync;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Wraps a slot and handles interactions for phantom slots.
 * Use {@link ModularSlot} directly.
 */
public class ItemSlotSH extends SyncHandler {

    private final ModularSlot slot;
    private ItemStack lastStoredItem;
    private ItemStack lastStoredPhantomItem = ItemStack.EMPTY;
    private boolean registered = false;

    @ApiStatus.Internal
    public ItemSlotSH(ModularSlot slot) {
        this.slot = slot;
    }

    @Override
    public void init(String key, PanelSyncManager syncHandler) {
        super.init(key, syncHandler);
        if (!registered) {
            getSyncManager().getContainer().registerSlot(getSyncManager().getPanelName(), this.slot);
            this.registered = true;
        }
        this.lastStoredItem = getSlot().getStack().copy();
        if (isPhantom() && !getSlot().getStack().isEmpty()) {
            this.lastStoredPhantomItem = getSlot().getStack().copy();
            this.lastStoredPhantomItem.setCount(1);
        }
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        ItemStack itemStack = getSlot().getStack();
        if (itemStack.isEmpty() && this.lastStoredItem.isEmpty()) return;
        boolean onlyAmountChanged = false;
        if (init ||
                !ItemHandlerHelper.canItemStacksStack(this.lastStoredItem, itemStack) ||
                (onlyAmountChanged = itemStack.getCount() != this.lastStoredItem.getCount())) {
            getSlot().onSlotChangedReal(itemStack, onlyAmountChanged, false, init);
            if (onlyAmountChanged) {
                this.lastStoredItem.setCount(itemStack.getCount());
            } else {
                this.lastStoredItem = itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copy();
            }
            final boolean finalOnlyAmountChanged = onlyAmountChanged;
            syncToClient(1, buffer -> {
                buffer.writeBoolean(finalOnlyAmountChanged);
                buffer.writeItemStack(itemStack);
                buffer.writeBoolean(init);
            });
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            boolean onlyAmountChanged = buf.readBoolean();
            this.lastStoredItem = NetworkUtils.readItemStack(buf);
            getSlot().onSlotChangedReal(this.lastStoredItem, onlyAmountChanged, true, buf.readBoolean());
        } else if (id == 4) {
            setEnabled(buf.readBoolean(), false);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == 2) {
            phantomClick(MouseData.readPacket(buf));
        } else if (id == 3) {
            phantomScroll(MouseData.readPacket(buf));
        } else if (id == 4) {
            setEnabled(buf.readBoolean(), false);
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
            stackToPut.setCount(Math.min(stackToPut.getCount(), slot.getItemStackLimit(stackToPut)));
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
            stackToPut.setCount(Math.min(stackToPut.getCount(), slot.getItemStackLimit(stackToPut)));
            getSlot().putStack(stackToPut);
            this.lastStoredPhantomItem = stackToPut.copy();
        } else {
            if (mouseData.mouseButton == 0) {
                if (mouseData.shift) {
                    this.slot.putStack(ItemStack.EMPTY);
                } else {
                    incrementStackCount(-1);
                }
            } else if (mouseData.mouseButton == 1) {
                incrementStackCount(1);
            }
        }
    }

    protected void phantomScroll(MouseData mouseData) {
        ItemStack currentItem = this.slot.getStack();
        int amount = mouseData.mouseButton;
        if (mouseData.shift) amount *= 4;
        if (mouseData.ctrl) amount *= 16;
        if (mouseData.alt) amount *= 64;
        if (amount > 0 && currentItem.isEmpty() && !this.lastStoredPhantomItem.isEmpty()) {
            ItemStack stackToPut = this.lastStoredPhantomItem.copy();
            stackToPut.setCount(amount);
            this.slot.putStack(stackToPut);
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
                if (!this.slot.isIgnoreMaxStackSize() && stack.getMaxStackSize() < maxSize) {
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

    public void setEnabled(boolean enabled, boolean sync) {
        this.slot.setEnabled(enabled);
        if (sync) {
            sync(4, buffer -> buffer.writeBoolean(enabled));
        }
    }

    public void updateFromClient(ItemStack stack) {
        syncToServer(5, buf -> buf.writeItemStack(stack));
    }

    public ModularSlot getSlot() {
        return this.slot;
    }

    public boolean isItemValid(ItemStack itemStack) {
        return getSlot().isItemValid(itemStack);
    }

    public boolean isPhantom() {
        return this.slot.isPhantom();
    }

    @Nullable
    public String getSlotGroup() {
        return this.slot.getSlotGroupName();
    }
}
