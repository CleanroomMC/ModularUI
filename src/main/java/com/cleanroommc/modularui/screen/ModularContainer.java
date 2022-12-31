package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.ItemSlotSH;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ModularContainer extends Container {

    public static ModularContainer getCurrent(EntityPlayer player) {
        Container container = player.openContainer;
        if (container instanceof ModularContainer) {
            return (ModularContainer) container;
        }
        return null;
    }

    private final GuiSyncHandler guiSyncHandler;
    private boolean init = true;
    private final List<ItemSlotSH> slots = new ArrayList<>();
    private final List<ItemSlotSH> shiftClickSlots = new ArrayList<>();

    public ModularContainer(GuiSyncHandler guiSyncHandler) {
        this.guiSyncHandler = Objects.requireNonNull(guiSyncHandler);
        this.guiSyncHandler.construct(this);
        sortShiftClickSlots();
    }

    @SideOnly(Side.CLIENT)
    public ModularContainer() {
        this.guiSyncHandler = null;
    }

    @Override
    public void detectAndSendChanges() {
        this.guiSyncHandler.detectAndSendChanges(this.init);
        this.init = false;
    }

    private void sortShiftClickSlots() {
        this.shiftClickSlots.sort(Comparator.comparingInt(ItemSlotSH::getShiftClickPriority));
    }

    public void registerSlot(ItemSlotSH syncHandler) {
        Slot slot = syncHandler.getSlot();
        if (this.inventorySlots.contains(slot)) {
            throw new IllegalArgumentException();
        }
        slot = addSlotToContainer(slot);
        this.slots.add(syncHandler);
        if (syncHandler.isAllowShiftClick()) {
            this.shiftClickSlots.add(syncHandler);
        }
        if (!this.init) {
            sortShiftClickSlots();
        }
    }

    public GuiSyncHandler getSyncHandler() {
        if (this.guiSyncHandler == null) {
            throw new IllegalStateException("GuiSyncHandler is not available for client only GUI's.");
        }
        return guiSyncHandler;
    }

    public boolean isClient() {
        return this.guiSyncHandler == null || NetworkUtils.isClient(this.guiSyncHandler.getPlayer());
    }

    public boolean isClientOnly() {
        return this.guiSyncHandler == null;
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return true;
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        ItemSlotSH slot = this.slots.get(index);
        if (!slot.isPhantom()) {
            ItemStack stack = slot.getSlot().getStack();
            if (!stack.isEmpty()) {
                ItemStack remainder = transferItem(slot, stack.copy());
                stack.setCount(remainder.getCount());
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack transferItem(ItemSlotSH fromSlot, ItemStack stack) {
        for (ItemSlotSH slot : this.shiftClickSlots) {
            if (fromSlot.getShiftClickPriority() != slot.getShiftClickPriority() && slot.getSlot().isEnabled() && slot.isItemValid(stack)) {
                ItemStack itemstack = slot.getSlot().getStack();
                if (slot.isPhantom()) {
                    if (itemstack.isEmpty() || (ItemHandlerHelper.canItemStacksStackRelaxed(stack, itemstack) && itemstack.getCount() < slot.getSlot().getItemStackLimit(itemstack))) {
                        slot.getSlot().putStack(stack.copy());
                        return stack;
                    }
                } else {
                    if (ItemHandlerHelper.canItemStacksStackRelaxed(stack, itemstack)) {
                        int j = itemstack.getCount() + stack.getCount();
                        int maxSize = Math.min(slot.getSlot().getSlotStackLimit(), stack.getMaxStackSize());

                        if (j <= maxSize) {
                            stack.setCount(0);
                            itemstack.setCount(j);
                            slot.getSlot().onSlotChanged();
                        } else if (itemstack.getCount() < maxSize) {
                            stack.shrink(maxSize - itemstack.getCount());
                            itemstack.setCount(maxSize);
                            slot.getSlot().onSlotChanged();
                        }

                        if (stack.isEmpty()) {
                            return stack;
                        }
                    }
                }
            }
        }
        for (ItemSlotSH syncHandler : this.shiftClickSlots) {
            Slot slot = syncHandler.getSlot();
            ItemStack itemstack = slot.getStack();
            boolean different;
            if (fromSlot.getSlot().getClass() != slot.getClass()) {
                different = true;
            } else if (fromSlot.getSlot() instanceof SlotItemHandler && slot instanceof SlotItemHandler) {
                different = ((SlotItemHandler) fromSlot.getSlot()).getItemHandler() != ((SlotItemHandler) slot).getItemHandler();
            } else {
                different = fromSlot.getSlot().inventory != slot.inventory;
            }

            if (different && slot.isEnabled() && itemstack.isEmpty() && slot.isItemValid(stack)) {
                if (stack.getCount() > slot.getSlotStackLimit()) {
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
