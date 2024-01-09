package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.core.mixin.ContainerAccessor;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
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
        if (player.openContainer instanceof ModularContainer container) {
            return container;
        }
        return null;
    }

    private final GuiSyncManager guiSyncManager;
    private boolean init = true;
    private final List<ModularSlot> slots = new ArrayList<>();
    private final List<ModularSlot> shiftClickSlots = new ArrayList<>();

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;

    public ModularContainer(GuiSyncManager guiSyncManager) {
        this.guiSyncManager = Objects.requireNonNull(guiSyncManager);
        this.guiSyncManager.construct(this);
        sortShiftClickSlots();
    }

    @SideOnly(Side.CLIENT)
    public ModularContainer() {
        this.guiSyncManager = null;
    }

    public ContainerAccessor acc() {
        return (ContainerAccessor) this;
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
            throw new IllegalArgumentException("Tried to register slot which already exists!");
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
    public @NotNull ItemStack slotClick(int slotId, int mouseButton, @NotNull ClickType clickTypeIn, EntityPlayer player) {
        ItemStack returnable = ItemStack.EMPTY;
        InventoryPlayer inventoryplayer = player.inventory;

        if (clickTypeIn == ClickType.QUICK_CRAFT || acc().getDragEvent() != 0) {
            return super.slotClick(slotId, mouseButton, clickTypeIn, player);
        }

        if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) &&
                (mouseButton == LEFT_MOUSE || mouseButton == RIGHT_MOUSE)) {
            if (slotId == DROP_TO_WORLD) {
                if (!inventoryplayer.getItemStack().isEmpty()) {
                    if (mouseButton == LEFT_MOUSE) {
                        player.dropItem(inventoryplayer.getItemStack(), true);
                        inventoryplayer.setItemStack(ItemStack.EMPTY);
                    }

                    if (mouseButton == RIGHT_MOUSE) {
                        player.dropItem(inventoryplayer.getItemStack().splitStack(1), true);
                    }
                }
                return inventoryplayer.getItemStack();
            }
            if (clickTypeIn == ClickType.QUICK_MOVE) {
                Slot fromSlot = getSlot(slotId);

                if (!fromSlot.canTakeStack(player)) {
                    return ItemStack.EMPTY;
                }

                returnable = this.transferStackInSlot(player, slotId);
            } else {
                if (slotId < 0) return ItemStack.EMPTY;
                Slot clickedSlot = getSlot(slotId);

                ItemStack slotStack = clickedSlot.getStack();
                ItemStack heldStack = inventoryplayer.getItemStack();

                if (slotStack.isEmpty()) {
                    if (!heldStack.isEmpty() && clickedSlot.isItemValid(heldStack)) {
                        int stackCount = mouseButton == LEFT_MOUSE ? heldStack.getCount() : 1;

                        if (stackCount > clickedSlot.getItemStackLimit(heldStack)) {
                            stackCount = clickedSlot.getItemStackLimit(heldStack);
                        }

                        clickedSlot.putStack(heldStack.splitStack(stackCount));
                    }
                } else if (clickedSlot.canTakeStack(player)) {
                    if (heldStack.isEmpty() && !slotStack.isEmpty()) {
                        int toRemove = mouseButton == LEFT_MOUSE ? slotStack.getCount() : (slotStack.getCount() + 1) / 2;
                        inventoryplayer.setItemStack(slotStack.splitStack(toRemove));
                        clickedSlot.putStack(slotStack);

                        clickedSlot.onTake(player, inventoryplayer.getItemStack());
                    } else if (clickedSlot.isItemValid(heldStack)) {
                        if (slotStack.getItem() == heldStack.getItem() &&
                                slotStack.getMetadata() == heldStack.getMetadata() &&
                                ItemStack.areItemStackTagsEqual(slotStack, heldStack)) {
                            int stackCount = mouseButton == LEFT_MOUSE ? heldStack.getCount() : 1;

                            if (stackCount > clickedSlot.getItemStackLimit(heldStack) - slotStack.getCount()) {
                                stackCount = clickedSlot.getItemStackLimit(heldStack) - slotStack.getCount();
                            }

                            if (stackCount > heldStack.getMaxStackSize() - slotStack.getCount()) {
                                stackCount = heldStack.getMaxStackSize() - slotStack.getCount();
                            }

                            heldStack.shrink(stackCount);
                            slotStack.grow(stackCount);
                            clickedSlot.putStack(slotStack);

                        } else if (heldStack.getCount() <= clickedSlot.getItemStackLimit(heldStack)) {
                            clickedSlot.putStack(heldStack);
                            inventoryplayer.setItemStack(slotStack);
                        }
                    } else if (slotStack.getItem() == heldStack.getItem() &&
                            heldStack.getMaxStackSize() > 1 &&
                            (!slotStack.getHasSubtypes() || slotStack.getMetadata() == heldStack.getMetadata()) &&
                            ItemStack.areItemStackTagsEqual(slotStack, heldStack) && !slotStack.isEmpty()) {
                        int stackCount = slotStack.getCount();

                        if (stackCount + heldStack.getCount() <= heldStack.getMaxStackSize()) {
                            heldStack.grow(stackCount);
                            slotStack = clickedSlot.decrStackSize(stackCount);

                            if (slotStack.isEmpty()) {
                                clickedSlot.putStack(ItemStack.EMPTY);
                            }

                            clickedSlot.onTake(player, inventoryplayer.getItemStack());
                        }
                    }
                }
                clickedSlot.onSlotChanged();
            }
            this.detectAndSendChanges();
            return returnable;
        }

        return super.slotClick(slotId, mouseButton, clickTypeIn, player);
    }


    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        ModularSlot slot = this.slots.get(index);
        if (!slot.isPhantom()) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                ItemStack remainder = transferItem(slot, stack.copy());
                if (remainder.isEmpty()) stack = ItemStack.EMPTY;
                else stack.setCount(remainder.getCount());
                slot.putStack(stack);
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack transferItem(ModularSlot fromSlot, ItemStack fromStack) {
        @Nullable SlotGroup fromSlotGroup = fromSlot.getSlotGroup();
        for (ModularSlot toSlot : this.shiftClickSlots) {
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && toSlot.isEnabled() && toSlot.isItemValid(fromStack)) {
                ItemStack toStack = toSlot.getStack().copy();
                if (toSlot.isPhantom()) {
                    if (toStack.isEmpty() || (ItemHandlerHelper.canItemStacksStack(fromStack, toStack) && toStack.getCount() < toSlot.getItemStackLimit(toStack))) {
                        toSlot.putStack(fromStack.copy());
                        return fromStack;
                    }
                } else if (ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {
                    int j = toStack.getCount() + fromStack.getCount();
                    int maxSize = Math.min(toSlot.getSlotStackLimit(), fromStack.getMaxStackSize());

                    if (j <= maxSize) {
                        fromStack.setCount(0);
                        toStack.setCount(j);
                        toSlot.putStack(toStack);
                    } else if (toStack.getCount() < maxSize) {
                        fromStack.shrink(maxSize - toStack.getCount());
                        toStack.setCount(maxSize);
                        toSlot.putStack(toStack);
                    }

                    if (fromStack.isEmpty()) {
                        return fromStack;
                    }
                }
            }
        }
        for (ModularSlot emptySlot : this.shiftClickSlots) {
            ItemStack itemstack = emptySlot.getStack();
            SlotGroup slotGroup = Objects.requireNonNull(emptySlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && emptySlot.isEnabled() && itemstack.isEmpty() && emptySlot.isItemValid(fromStack)) {
                if (fromStack.getCount() > emptySlot.getSlotStackLimit()) {
                    emptySlot.putStack(fromStack.splitStack(emptySlot.getSlotStackLimit()));
                } else {
                    emptySlot.putStack(fromStack.splitStack(fromStack.getCount()));
                }
                if (fromStack.getCount() < 1) {
                    break;
                }
            }
        }
        return fromStack;
    }

    private static boolean isPlayerSlot(Slot slot) {
        if (slot == null) return false;
        if (slot.inventory instanceof InventoryPlayer) {
            return slot.getSlotIndex() >= 0 && slot.getSlotIndex() < 36;
        }
        if (slot instanceof SlotItemHandler slotItemHandler) {
            IItemHandler iItemHandler = slotItemHandler.getItemHandler();
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
