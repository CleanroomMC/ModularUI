package com.cleanroommc.modularui.widgets.slot;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * The base class for slots in a modular ui.
 * It represents an interface between a player (via gui) and a slot in a {@link IItemHandler} that exists
 * on server and client.
 */
public class ModularSlot extends SlotItemHandler {

    @Getter private final boolean phantom;
    private boolean enabled = true;
    private boolean canTake = true, canPut = true;
    private Predicate<ItemStack> filter = stack -> true;
    private IOnSlotChanged changeListener = IOnSlotChanged.DEFAULT;
    @Getter private boolean ignoreMaxStackSize = false;
    private String slotGroupName = null;
    private SlotGroup slotGroup = null;

    public ModularSlot(IItemHandler itemHandler, int index) {
        this(itemHandler, index, false);
    }

    /**
     * Creates a ModularSlot
     *
     * @param itemHandler item handler of the slot
     * @param index       slot index in the item handler
     * @param phantom     true if the slot should not be a real slot, but only a phantom of the item that would be in the slot
     */
    public ModularSlot(IItemHandler itemHandler, int index, boolean phantom) {
        super(itemHandler, index, Integer.MIN_VALUE, Integer.MIN_VALUE);
        this.phantom = phantom;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return this.canPut && !stack.isEmpty() && this.filter.test(stack) && super.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return this.canTake && super.mayPickup(playerIn);
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return this.ignoreMaxStackSize ? getMaxStackSize() : super.getMaxStackSize(stack);
    }

    @Override
    public void setChanged() {
    }

    public void onSlotChangedReal(ItemStack itemStack, boolean onlyChangedAmount, boolean client, boolean init) {
        this.changeListener.onChange(itemStack, onlyChangedAmount, client, init);
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        if (ItemStack.isSameItem(stack, getItem())) return; // TODO: isSameItemSameTags?
        super.set(stack);
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return null;
    }

    @ApiStatus.Internal
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isActive() {
        return this.enabled;
    }

    @Nullable
    public String getSlotGroupName() {
        return this.slotGroupName;
    }

    @Nullable
    public SlotGroup getSlotGroup() {
        return this.slotGroup;
    }

    /**
     * Sets a filter. The predicate is called every time someone tries to insert something via the gui.
     *
     * @param filter the predicate to test on every item
     */
    public ModularSlot filter(Predicate<ItemStack> filter) {
        this.filter = filter != null ? filter : stack -> true;
        return this;
    }

    /**
     * Sets a change listener that is called every time the item in this slot changes, with the new item as argument.
     * ! It is not guaranteed that the new item is different from the old one.
     *
     * @param changeListener change listener that should be called on a change
     */
    public ModularSlot changeListener(IOnSlotChanged changeListener) {
        this.changeListener = changeListener != null ? changeListener : IOnSlotChanged.DEFAULT;
        return this;
    }

    /**
     * Sets if items can be taken or put into this slot via the gui.
     * ! It does NOT affect transfers via pipes and the likes!
     *
     * @param canPut  if items can be put into the slot via the gui
     * @param canTake if items can be taken from the slot via the gui
     */
    public ModularSlot accessibility(boolean canPut, boolean canTake) {
        this.canPut = canPut;
        this.canTake = canTake;
        return this;
    }

    /**
     * Sets if the max stack size of items should be ignored. Only item handler slot limit matters if true.
     *
     * @param ignoreMaxStackSize if max stack size should be ignored
     */
    @ApiStatus.Experimental
    public ModularSlot ignoreMaxStackSize(boolean ignoreMaxStackSize) {
        this.ignoreMaxStackSize = ignoreMaxStackSize;
        return this;
    }

    /**
     * Sets a slot group for this slot by a name. The slot group must be registered.
     * The real slot group is later automatically set.
     *
     * @param slotGroup slot group id
     */
    public ModularSlot slotGroup(String slotGroup) {
        this.slotGroupName = slotGroup;
        return this;
    }

    /**
     * Sets a slot group for this slot. The slot group must be registered if it's not a singleton.
     *
     * @param slotGroup slot group
     */
    public ModularSlot slotGroup(SlotGroup slotGroup) {
        if (this.slotGroup == slotGroup) return this;
        if (this.slotGroup != null) {
            this.slotGroup.removeSlot(this);
        }
        this.slotGroup = slotGroup;
        if (this.slotGroup != null) {
            this.slotGroup.addSlot(this);
        }
        return this;
    }

    /**
     * Creates and sets a singleton slot group simply for the purpose of shift clicking into slots that don't belong to a group.
     *
     * @param shiftClickPriority determines in which group a shift clicked item should be inserted first
     */
    public ModularSlot singletonSlotGroup(int shiftClickPriority) {
        this.slotGroupName = null;
        return slotGroup(SlotGroup.singleton(toString(), shiftClickPriority));
    }

    /**
     * Creates and sets a singleton slot group simply for the purpose of shift clicking into slots that don't belong to a group.
     */
    public ModularSlot singletonSlotGroup() {
        return singletonSlotGroup(SlotGroup.STORAGE_SLOT_PRIO);
    }
}
