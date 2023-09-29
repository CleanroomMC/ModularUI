package com.cleanroommc.modularui.screen;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private final GuiSyncManager guiSyncManager;
    private boolean init = true;
    private final List<ModularSlot> slots = new ArrayList<>();
    private final List<ModularSlot> shiftClickSlots = new ArrayList<>();

    public ModularContainer(GuiSyncManager guiSyncManager) {
        this.guiSyncManager = Objects.requireNonNull(guiSyncManager);
        this.guiSyncManager.construct(this);
        sortShiftClickSlots();
    }

    @SideOnly(Side.CLIENT)
    public ModularContainer() {
        this.guiSyncManager = null;
    }

    @Override
    public void onContainerClosed(@NotNull EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (this.guiSyncManager != null) {
            this.guiSyncManager.onClose();
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.guiSyncManager.detectAndSendChanges(this.init);
        this.init = false;
    }

    private void sortShiftClickSlots() {
        this.shiftClickSlots.sort(Comparator.comparingInt(slot -> Objects.requireNonNull(slot.getSlotGroup()).getShiftClickPriority()));
    }

    @Override
    public void setAll(@NotNull List<ItemStack> items) {
        if (this.inventorySlots.size() != items.size()) {
            ModularUI.LOGGER.error("Here are {} slots, but expected {}", this.inventorySlots.size(), items.size());
        }
        for (int i = 0; i < Math.min(this.inventorySlots.size(), items.size()); ++i) {
            this.getSlot(i).putStack(items.get(i));
        }
    }

    @ApiStatus.Internal
    public void registerSlot(ModularSlot slot) {
        if (this.inventorySlots.contains(slot)) {
            throw new IllegalArgumentException();
        }
        addSlotToContainer(slot);
        this.slots.add(slot);
        if (slot.getSlotGroupName() != null) {
            SlotGroup slotGroup = getSyncManager().getSlotGroup(slot.getSlotGroupName());
            if (slotGroup == null) {
                ModularUI.LOGGER.throwing(new IllegalArgumentException("SlotGroup '" + slot.getSlotGroupName() + "' is not registered!"));
                return;
            }
            slot.slotGroup(slotGroup);
        }
        if (slot.getSlotGroup() != null) {
            SlotGroup slotGroup = slot.getSlotGroup();
            if (slotGroup.allowShiftTransfer()) {
                this.shiftClickSlots.add(slot);
                if (!this.init) {
                    sortShiftClickSlots();
                }
            }
        }
    }

    @Contract("null, null -> fail")
    @NotNull
    @ApiStatus.Internal
    public SlotGroup validateSlotGroup(@Nullable String slotGroupName, @Nullable SlotGroup slotGroup) {
        if (slotGroup != null) {
            if (getSyncManager().getSlotGroup(slotGroup.getName()) == null) {
                throw new IllegalArgumentException("Slot group is not registered in the GUI.");
            }
            return slotGroup;
        }
        if (slotGroupName != null) {
            slotGroup = getSyncManager().getSlotGroup(slotGroupName);
            if (slotGroup == null) {
                throw new IllegalArgumentException("Can't find slot group for name " + slotGroupName);
            }
            return slotGroup;
        }
        throw new IllegalArgumentException("Either the slot group or the name must not be null!");
    }


    public GuiSyncManager getSyncManager() {
        if (this.guiSyncManager == null) {
            throw new IllegalStateException("GuiSyncManager is not available for client only GUI's.");
        }
        return this.guiSyncManager;
    }

    public boolean isClient() {
        return this.guiSyncManager == null || NetworkUtils.isClient(this.guiSyncManager.getPlayer());
    }

    public boolean isClientOnly() {
        return this.guiSyncManager == null;
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return true;
    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        ModularSlot slot = this.slots.get(index);
        if (!slot.isPhantom()) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                ItemStack remainder = transferItem(slot, stack.copy());
                stack.setCount(remainder.getCount());
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack transferItem(ModularSlot fromSlot, ItemStack stack) {
        @Nullable SlotGroup fromSlotGroup = fromSlot.getSlotGroup();
        for (ModularSlot slot : this.shiftClickSlots) {
            SlotGroup slotGroup = Objects.requireNonNull(slot.getSlotGroup());
            if (slotGroup != fromSlotGroup && slot.isEnabled() && slot.isItemValid(stack)) {
                ItemStack itemstack = slot.getStack();
                if (slot.isPhantom()) {
                    if (itemstack.isEmpty() || (ItemHandlerHelper.canItemStacksStack(stack, itemstack) && itemstack.getCount() < slot.getItemStackLimit(itemstack))) {
                        slot.putStack(stack.copy());
                        return stack;
                    }
                } else if (ItemHandlerHelper.canItemStacksStack(stack, itemstack)) {
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
        for (ModularSlot slot : this.shiftClickSlots) {
            ItemStack itemstack = slot.getStack();
            SlotGroup slotGroup = Objects.requireNonNull(slot.getSlotGroup());
            if (slotGroup != fromSlotGroup && slot.isEnabled() && itemstack.isEmpty() && slot.isItemValid(stack)) {
                if (stack.getCount() > slot.getSlotStackLimit()) {
                    slot.putStack(stack.splitStack(slot.getSlotStackLimit()));
                } else {
                    slot.putStack(stack.splitStack(stack.getCount()));
                }
                if (stack.getCount() < 1) {
                    break;
                }
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
        for (SlotGroup slotGroup : this.getSyncManager().getSlotGroups()) {
            if (slotGroup.isAllowSorting() && !isPlayerSlot(slotGroup.getSlots().get(0))) {
                builder.addSlotGroupOf(slotGroup.getSlots(), slotGroup.getRowSize())
                        .buttonPosSetter(null)
                        .priority(slotGroup.getShiftClickPriority());
            }
        }
    }

    @Override
    public IPosSetter getPlayerButtonPosSetter() {
        return null;
    }
}
