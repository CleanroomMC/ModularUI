package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Sometimes you need special behaviour. This class allows you to override common methods from {@link net.minecraft.world.Container Container}.
 * <br>
 * <b>NOTE: Only use this if you know what you are doing. You can do a lot wrong here and cause stupid bugs.</b>
 */
public class ContainerCustomizer {

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;

    private ModularContainer container;

    void initialize(ModularContainer container) {
        this.container = container;
    }

    public ModularContainer getContainer() {
        if (container == null) {
            throw new NullPointerException("ContainerCustomizer is not registered!");
        }
        return container;
    }

    public void onContainerClosed() {}

    public void slotClick(int slotId, int mouseButton, @NotNull ClickType clickTypeIn, Player player) {
        ItemStack returnable = ItemStack.EMPTY;
        Inventory inventoryplayer = player.getInventory();

        if (clickTypeIn == ClickType.QUICK_CRAFT || container.acc().getQuickcraftStatus() != 0) {
            container.superSlotClick(slotId, mouseButton, clickTypeIn, player);
            return;
        }

        if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) &&
                (mouseButton == LEFT_MOUSE || mouseButton == RIGHT_MOUSE)) {
            if (slotId == DROP_TO_WORLD) {
                if (!inventoryplayer.getSelected().isEmpty()) {
                    if (mouseButton == LEFT_MOUSE) {
                        player.drop(inventoryplayer.getSelected(), true);
                        inventoryplayer.setPickedItem(ItemStack.EMPTY);
                    }

                    if (mouseButton == RIGHT_MOUSE) {
                        player.drop(inventoryplayer.getSelected().split(1), true);
                    }
                }
                return;
            }

            if (slotId < 0) return;

            if (clickTypeIn == ClickType.QUICK_MOVE) {
                Slot fromSlot = container.getSlot(slotId);

                if (!fromSlot.mayPickup(player)) {
                    return;
                }

                returnable = transferStackInSlot(player, slotId);
            } else {
                Slot clickedSlot = container.getSlot(slotId);

                ItemStack slotStack = clickedSlot.getItem();
                ItemStack heldStack = inventoryplayer.getSelected();

                if (slotStack.isEmpty()) {
                    if (!heldStack.isEmpty() && clickedSlot.mayPlace(heldStack)) {
                        int stackCount = mouseButton == LEFT_MOUSE ? heldStack.getCount() : 1;

                        if (stackCount > clickedSlot.getMaxStackSize(heldStack)) {
                            stackCount = clickedSlot.getMaxStackSize(heldStack);
                        }

                        clickedSlot.set(heldStack.split(stackCount));
                    }
                } else if (clickedSlot.mayPickup(player)) {
                    if (heldStack.isEmpty() && !slotStack.isEmpty()) {
                        int s = Math.min(slotStack.getCount(), slotStack.getMaxStackSize());
                        int toRemove = mouseButton == LEFT_MOUSE ? s : (s + 1) / 2;
                        inventoryplayer.setPickedItem(slotStack.split(toRemove));
                        clickedSlot.set(slotStack);
                        clickedSlot.onTake(player, inventoryplayer.getSelected());
                    } else if (clickedSlot.mayPlace(heldStack)) {
                        if (slotStack.getItem() == heldStack.getItem() &&
                                ItemStack.isSameItemSameTags(slotStack, heldStack)) {
                            int stackCount = mouseButton == LEFT_MOUSE ? heldStack.getCount() : 1;

                            if (stackCount > clickedSlot.getMaxStackSize(heldStack) - slotStack.getCount()) {
                                stackCount = clickedSlot.getMaxStackSize(heldStack) - slotStack.getCount();
                            }

                            heldStack.shrink(stackCount);
                            slotStack.grow(stackCount);
                            clickedSlot.set(slotStack);

                        } else if (heldStack.getCount() <= clickedSlot.getMaxStackSize(heldStack)) {
                            clickedSlot.set(heldStack);
                            inventoryplayer.setPickedItem(slotStack);
                        }
                    } else if (slotStack.getItem() == heldStack.getItem() &&
                            heldStack.getMaxStackSize() > 1 &&
                            ItemStack.isSameItemSameTags(slotStack, heldStack) && !slotStack.isEmpty()) {
                        int stackCount = slotStack.getCount();

                        if (stackCount + heldStack.getCount() <= heldStack.getMaxStackSize()) {
                            heldStack.grow(stackCount);
                            slotStack = clickedSlot.remove(stackCount);

                            if (slotStack.isEmpty()) {
                                clickedSlot.set(ItemStack.EMPTY);
                            }

                            clickedSlot.onTake(player, inventoryplayer.getSelected());
                        }
                    }
                }
                clickedSlot.setChanged();
            }
            container.sendAllDataToRemote();
            return;
        } else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot = container.slots.get(slotId);
            ItemStack itemstack1 = inventoryplayer.getSelected();

            if (!itemstack1.isEmpty() && (slot == null || !slot.hasItem() || !slot.mayPickup(player))) {
                int i = mouseButton == 0 ? 0 : container.slots.size() - 1;
                int j = mouseButton == 0 ? 1 : -1;

                for (int k = 0; k < 2; ++k) {
                    for (int l = i; l >= 0 && l < container.slots.size() && itemstack1.getCount() < itemstack1.getMaxStackSize(); l += j) {
                        Slot slot1 = container.slots.get(l);
                        if (slot1 instanceof ModularSlot modularSlot && modularSlot.isPhantom()) continue;

                        if (slot1.hasItem() && AbstractContainerMenu.canItemQuickReplace(slot1, itemstack1, true) && slot1.mayPickup(player) && canMergeSlot(itemstack1, slot1)) {
                            ItemStack itemstack2 = slot1.getItem();

                            if (k != 0 || itemstack2.getCount() != itemstack2.getMaxStackSize()) {
                                int i1 = Math.min(itemstack1.getMaxStackSize() - itemstack1.getCount(), itemstack2.getCount());
                                ItemStack itemstack3 = slot1.remove(i1);
                                itemstack1.grow(i1);

                                if (itemstack3.isEmpty()) {
                                    slot1.set(ItemStack.EMPTY);
                                }

                                slot1.onTake(player, itemstack3);
                            }
                        }
                    }
                }
            }

            container.sendAllDataToRemote();
            return;
        }

        container.superSlotClick(slotId, mouseButton, clickTypeIn, player);
    }

    public @NotNull ItemStack transferStackInSlot(@NotNull Player playerIn, int index) {
        ModularSlot slot = this.container.getModularSlot(index);
        if (!slot.isPhantom()) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                stack = stack.copy();
                int base = 0;
                if (stack.getCount() > stack.getMaxStackSize()) {
                    base = stack.getCount() - stack.getMaxStackSize();
                    stack.setCount(stack.getMaxStackSize());
                }
                ItemStack remainder = transferItem(slot, stack.copy());
                if (base == 0 && remainder.isEmpty()) stack = ItemStack.EMPTY;
                else stack.setCount(base + remainder.getCount());
                slot.set(stack);
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack transferItem(ModularSlot fromSlot, ItemStack fromStack) {
        @Nullable SlotGroup fromSlotGroup = fromSlot.getSlotGroup();
        for (ModularSlot toSlot : this.container.getShiftClickSlots()) {
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && toSlot.isActive() && toSlot.mayPlace(fromStack)) {
                ItemStack toStack = toSlot.getItem().copy();
                if (toSlot.isPhantom()) {
                    if (toStack.isEmpty() || (ItemHandlerHelper.canItemStacksStack(fromStack, toStack) && toStack.getCount() < toSlot.getMaxStackSize(toStack))) {
                        toSlot.set(fromStack.copy());
                        return fromStack;
                    }
                } else if (ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {
                    int j = toStack.getCount() + fromStack.getCount();
                    int maxSize = toSlot.getMaxStackSize(fromStack);//Math.min(toSlot.getSlotStackLimit(), fromStack.getMaxStackSize());

                    if (j <= maxSize) {
                        fromStack.setCount(0);
                        toStack.setCount(j);
                        toSlot.set(toStack);
                    } else if (toStack.getCount() < maxSize) {
                        fromStack.shrink(maxSize - toStack.getCount());
                        toStack.setCount(maxSize);
                        toSlot.set(toStack);
                    }

                    if (fromStack.isEmpty()) {
                        return fromStack;
                    }
                }
            }
        }
        for (ModularSlot emptySlot : this.container.getShiftClickSlots()) {
            ItemStack itemstack = emptySlot.getItem();
            SlotGroup slotGroup = Objects.requireNonNull(emptySlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && emptySlot.isActive() && itemstack.isEmpty() && emptySlot.mayPlace(fromStack)) {
                if (fromStack.getCount() > emptySlot.getMaxStackSize(fromStack)) {
                    emptySlot.set(fromStack.split(emptySlot.getMaxStackSize(fromStack)));
                } else {
                    emptySlot.set(fromStack.split(fromStack.getCount()));
                }
                if (fromStack.getCount() < 1) {
                    break;
                }
            }
        }
        return fromStack;
    }

    public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
        return this.container.superCanMergeSlot(stack, slotIn);
    }

    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return this.container.superMergeItemStack(stack, startIndex, endIndex, reverseDirection);
    }

    protected void clearContainer(Player playerIn, Container inventoryIn) {
        this.container.superClearContainer(playerIn, inventoryIn);
    }
}
