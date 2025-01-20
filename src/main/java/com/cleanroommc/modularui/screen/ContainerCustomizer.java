package com.cleanroommc.modularui.screen;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Sometimes you need special behaviour. This class allows you to override common methods from {@link net.minecraft.inventory.Container Container}.
 * <br>
 * <b>NOTE: Only use this if you know what you are doing. You can do a lot wrong here and cause stupid bugs.</b>
 */
public class ContainerCustomizer {

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;

    private ModularContainer container;
    private Predicate<EntityPlayer> canInteractWith;

    void initialize(ModularContainer container) {
        this.container = container;
    }

    public ModularContainer getContainer() {
        if (container == null) {
            throw new NullPointerException("ContainerCustomizer is not registered!");
        }
        return container;
    }

    public Predicate<EntityPlayer> getCanInteractWith() {
        return canInteractWith;
    }

    public void setCanInteractWith(Predicate<EntityPlayer> canInteractWith) {
        this.canInteractWith = canInteractWith;
    }

    public void onContainerClosed() {}

    public @NotNull ItemStack slotClick(int slotId, int mouseButton, @NotNull ClickType clickTypeIn, EntityPlayer player) {
        ItemStack returnable = ItemStack.EMPTY;
        InventoryPlayer inventoryplayer = player.inventory;

        if (clickTypeIn == ClickType.QUICK_CRAFT || container.acc().getDragEvent() != 0) {
            return container.superSlotClick(slotId, mouseButton, clickTypeIn, player);
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

            if (slotId < 0) return ItemStack.EMPTY;

            if (clickTypeIn == ClickType.QUICK_MOVE) {
                Slot fromSlot = container.getSlot(slotId);

                if (!fromSlot.canTakeStack(player)) {
                    return ItemStack.EMPTY;
                }

                returnable = transferStackInSlot(player, slotId);
            } else {
                Slot clickedSlot = container.getSlot(slotId);

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
                        int s = Math.min(slotStack.getCount(), slotStack.getMaxStackSize());
                        int toRemove = mouseButton == LEFT_MOUSE ? s : (s + 1) / 2;
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
            container.detectAndSendChanges();
            return returnable;
        } else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot = container.inventorySlots.get(slotId);
            ItemStack itemstack1 = inventoryplayer.getItemStack();

            if (!itemstack1.isEmpty() && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player))) {
                int i = mouseButton == 0 ? 0 : container.inventorySlots.size() - 1;
                int j = mouseButton == 0 ? 1 : -1;

                for (int k = 0; k < 2; ++k) {
                    for (int l = i; l >= 0 && l < container.inventorySlots.size() && itemstack1.getCount() < itemstack1.getMaxStackSize(); l += j) {
                        Slot slot1 = container.inventorySlots.get(l);
                        if (slot1 instanceof ModularSlot modularSlot && modularSlot.isPhantom()) continue;

                        if (slot1.getHasStack() && Container.canAddItemToSlot(slot1, itemstack1, true) && slot1.canTakeStack(player) && canMergeSlot(itemstack1, slot1)) {
                            ItemStack itemstack2 = slot1.getStack();

                            if (k != 0 || itemstack2.getCount() != itemstack2.getMaxStackSize()) {
                                int i1 = Math.min(itemstack1.getMaxStackSize() - itemstack1.getCount(), itemstack2.getCount());
                                ItemStack itemstack3 = slot1.decrStackSize(i1);
                                itemstack1.grow(i1);

                                if (itemstack3.isEmpty()) {
                                    slot1.putStack(ItemStack.EMPTY);
                                }

                                slot1.onTake(player, itemstack3);
                            }
                        }
                    }
                }
            }

            container.detectAndSendChanges();
            return returnable;
        } else if (clickTypeIn == ClickType.SWAP && mouseButton >= 0 && mouseButton < 9) {
            ModularSlot phantom = container.getModularSlot(slotId);
            ItemStack hotbarStack = inventoryplayer.getStackInSlot(mouseButton);
            if (phantom.isPhantom()) {
                // insert stack from hotbar slot into phantom slot
                phantom.putStack(hotbarStack.isEmpty() ? ItemStack.EMPTY : hotbarStack.copy());
                container.detectAndSendChanges();
                return returnable;
            }
        }

        return container.superSlotClick(slotId, mouseButton, clickTypeIn, player);
    }

    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        ModularSlot slot = this.container.getModularSlot(index);
        if (!slot.isPhantom()) {
            ItemStack stack = slot.getStack();
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
                slot.putStack(stack);
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack transferItem(ModularSlot fromSlot, ItemStack fromStack) {
        @Nullable SlotGroup fromSlotGroup = fromSlot.getSlotGroup();
        for (ModularSlot toSlot : this.container.getShiftClickSlots()) {
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
                    int maxSize = toSlot.getItemStackLimit(fromStack);//Math.min(toSlot.getSlotStackLimit(), fromStack.getMaxStackSize());

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
        for (ModularSlot emptySlot : this.container.getShiftClickSlots()) {
            ItemStack itemstack = emptySlot.getStack();
            SlotGroup slotGroup = Objects.requireNonNull(emptySlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && emptySlot.isEnabled() && itemstack.isEmpty() && emptySlot.isItemValid(fromStack)) {
                if (fromStack.getCount() > emptySlot.getItemStackLimit(fromStack)) {
                    emptySlot.putStack(fromStack.splitStack(emptySlot.getItemStackLimit(fromStack)));
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

    public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
        return this.container.superCanMergeSlot(stack, slotIn);
    }

    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        return this.container.superMergeItemStack(stack, startIndex, endIndex, reverseDirection);
    }

    protected void clearContainer(EntityPlayer playerIn, World worldIn, IInventory inventoryIn) {
        this.container.superClearContainer(playerIn, worldIn, inventoryIn);
    }
}
