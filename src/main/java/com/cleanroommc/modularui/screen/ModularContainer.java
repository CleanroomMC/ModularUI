package com.cleanroommc.modularui.screen;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Optional.Interface(modid = ModularUI.BOGO_SORT, iface = "com.cleanroommc.bogosorter.api.ISortableContainer")
public class ModularContainer extends Container implements ISortableContainer {

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
    public void setAll(@NotNull List<ItemStack> items) {
        if (this.inventorySlots.size() != items.size()) {
            ModularUI.LOGGER.error("Here are {} slots, but expected {}", inventorySlots.size(), items.size());
        }
        for (int i = 0; i < Math.min(this.inventorySlots.size(), items.size()); ++i) {
            this.getSlot(i).putStack(items.get(i));
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
        SlotGroup fromSlotGroup = getSlotGroup(fromSlot);
        for (ItemSlotSH slot : this.shiftClickSlots) {
            SlotGroup slotGroup = getSlotGroup(slot);
            boolean valid = slotGroup != null && slotGroup != fromSlotGroup;
            if (valid && slot.getSlot().isEnabled() && slot.isItemValid(stack)) {
                ItemStack itemstack = slot.getSlot().getStack();
                if (slot.isPhantom()) {
                    if (itemstack.isEmpty() || (ItemHandlerHelper.canItemStacksStackRelaxed(stack, itemstack) && itemstack.getCount() < slot.getSlot().getItemStackLimit(itemstack))) {
                        slot.getSlot().putStack(stack.copy());
                        return stack;
                    }
                } else if (ItemHandlerHelper.canItemStacksStackRelaxed(stack, itemstack)) {
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
        for (ItemSlotSH syncHandler : this.shiftClickSlots) {
            Slot slot = syncHandler.getSlot();
            ItemStack itemstack = slot.getStack();
            SlotGroup slotGroup = getSlotGroup(syncHandler);
            boolean valid = slotGroup != null && slotGroup != fromSlotGroup;
            if (valid && slot.isEnabled() && itemstack.isEmpty() && slot.isItemValid(stack)) {
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

    private static boolean isPlayerSlot(Slot slot) {
        if (slot == null) return false;
        if (slot.inventory instanceof InventoryPlayer) {
            return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
        }
        if (slot instanceof SlotItemHandler) {
            IItemHandler iItemHandler = ((SlotItemHandler) slot).getItemHandler();
            if (iItemHandler instanceof PlayerMainInvWrapper || iItemHandler instanceof PlayerInvWrapper) {
                return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
            }
        }
        return false;
    }

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        for (SlotGroup slotGroup : this.getSyncHandler().getSlotGroups()) {
            if (slotGroup.isAllowSorting()) {
                builder.addSlotGroup(slotGroup.getRowSize(), slotGroup.getSlots());
            }
        }
    }
}
