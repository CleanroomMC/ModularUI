package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.future.IItemHandler;
import com.cleanroommc.modularui.api.future.ItemHandlerHelper;
import com.cleanroommc.modularui.api.future.PlayerMainInvWrapper;
import com.cleanroommc.modularui.api.future.SlotItemHandler;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        super.detectAndSendChanges();
        this.guiSyncHandler.detectAndSendChanges(this.init);
        this.init = false;
    }

    public SlotGroup getSlotGroup(ItemSlotSH syncHandler) {
        if (syncHandler.getSlotGroup() == null) return null;
        return this.guiSyncHandler.getSlotGroup(syncHandler.getSlotGroup());
    }

    private void sortShiftClickSlots() {
        this.shiftClickSlots.sort(Comparator.comparingInt(slot -> getSlotGroup(slot).getShiftClickPriority()));
    }

    @Override
    public void putStacksInSlots(ItemStack[] items) {
        if (this.inventorySlots.size() != items.length) {
            ModularUI.LOGGER.error("Here are {} slots, but expected {}", inventorySlots.size(), items.length);
        }
        for (int i = 0; i < Math.min(this.inventorySlots.size(), items.length); ++i) {
            this.getSlot(i).putStack(items[i]);
        }
    }

    public void registerSlot(ItemSlotSH syncHandler) {
        Slot slot = syncHandler.getSlot();
        if (this.inventorySlots.contains(slot)) {
            throw new IllegalArgumentException();
        }
        addSlotToContainer(slot);
        this.slots.add(syncHandler);
        if (syncHandler.getSlotGroup() != null) {
            SlotGroup slotGroup = this.getSyncHandler().getSlotGroup(syncHandler.getSlotGroup());
            if (slotGroup == null) {
                ModularUI.LOGGER.throwing(new IllegalArgumentException("SlotGroup '" + syncHandler.getSlotGroup() + "' is not registered!"));
                return;
            }
            slotGroup.addSlot(slot);
            if (slotGroup.allowShiftTransfer()) {
                this.shiftClickSlots.add(syncHandler);
                if (!this.init) {
                    sortShiftClickSlots();
                }
            }
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
    @Nullable
    public ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        ItemSlotSH slot = this.slots.get(index);
        if (!slot.isPhantom()) {
            ItemStack stack = slot.getSlot().getStack();
            if (stack != null) {
                ItemStack remainder = transferItem(slot, stack.copy());
                stack.stackSize = remainder.stackSize;
                if (stack.stackSize < 1) {
                    slot.getSlot().putStack(null);
                }
                return null;
            }
        }
        return null;
    }

    // TODO: Don't insert to slot when a parent is disabled
    protected ItemStack transferItem(ItemSlotSH fromSlot, ItemStack stack) {
        SlotGroup fromSlotGroup = getSlotGroup(fromSlot);
        for (ItemSlotSH slot : this.shiftClickSlots) {
            SlotGroup slotGroup = getSlotGroup(slot);
            boolean valid = slotGroup != null && slotGroup != fromSlotGroup;
            // nh todo slot is enabled
            if (valid && slot.getSlot().func_111238_b() && slot.isItemValid(stack)) {
                ItemStack itemstack = slot.getSlot().getStack();
                if (slot.isPhantom()) {
                    if (itemstack == null || (ItemHandlerHelper.canItemStacksStack(stack, itemstack) && itemstack.stackSize < slot.getSlot().getSlotStackLimit())) {
                        slot.getSlot().putStack(stack.copy());
                        return stack;
                    }
                } else if (ItemHandlerHelper.canItemStacksStack(stack, itemstack)) {
                    int j = itemstack.stackSize + stack.stackSize;
                    int maxSize = Math.min(slot.getSlot().getSlotStackLimit(), stack.getMaxStackSize());

                    if (j <= maxSize) {
                        stack.stackSize = 0;
                        itemstack.stackSize = j;
                        slot.getSlot().onSlotChanged();
                    } else if (itemstack.stackSize < maxSize) {
                        stack.stackSize -= maxSize - itemstack.stackSize;
                        itemstack.stackSize = maxSize;
                        slot.getSlot().onSlotChanged();
                    }

                    if (stack.stackSize < 1) {
                        return stack;
                    }
                }
            }
        }
        for (ItemSlotSH syncHandler : this.shiftClickSlots) {
            Slot slot = syncHandler.getSlot();
            ItemStack itemstack = slot.getStack();
            SlotGroup slotGroup = getSlotGroup(syncHandler);
            boolean valid = slotGroup != null && slotGroup != fromSlotGroup;
            // nh todo slot is enabled
            if (valid && slot.func_111238_b() && itemstack == null && slot.isItemValid(stack)) {
                if (stack.stackSize > slot.getSlotStackLimit()) {
                    slot.putStack(stack.splitStack(slot.getSlotStackLimit()));
                } else {
                    slot.putStack(stack.splitStack(stack.stackSize));
                }
                break;
            }
        }
        return stack;
    }

    private static boolean isPlayerSlot(Slot slot) {
        if (slot == null) return false;
        if (slot.inventory instanceof InventoryPlayer) {
            return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
        }
        if (slot instanceof SlotItemHandler) {
            IItemHandler iItemHandler = ((SlotItemHandler) slot).getItemHandler();
            if (iItemHandler instanceof PlayerMainInvWrapper) {
                return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
            }
        }
        return false;
    }
}
