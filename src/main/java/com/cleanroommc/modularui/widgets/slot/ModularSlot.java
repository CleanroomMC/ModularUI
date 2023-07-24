package com.cleanroommc.modularui.widgets.slot;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The base class for slots in a modular ui.
 * It represents an interface between a player (via gui) and a slot in a {@link IItemHandler} that exists
 * on server and client.
 */
public class ModularSlot extends SlotItemHandler {

    private final boolean phantom;
    private boolean enabled = true;
    private boolean canTake = true, canPut = true;
    private Predicate<ItemStack> filter = stack -> true;
    private Consumer<ItemStack> changeListener = stack -> {
    };
    private boolean ignoreMaxStackSize = false;
    private String slotGroup = null;

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
    public boolean isItemValid(@NotNull ItemStack stack) {
        return this.canPut && !stack.isEmpty() && this.filter.test(stack) && super.isItemValid(stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return this.canTake && super.canTakeStack(playerIn);
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return this.ignoreMaxStackSize ? getSlotStackLimit() : super.getItemStackLimit(stack);
    }

    @Override
    public void onSlotChanged() {
        this.changeListener.accept(getStack());
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getBackgroundSprite() {
        return null;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isPhantom() {
        return this.phantom;
    }

    public boolean isIgnoreMaxStackSize() {
        return this.ignoreMaxStackSize;
    }

    @Nullable
    public String getSlotGroup() {
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
    public ModularSlot changeListener(Consumer<ItemStack> changeListener) {
        this.changeListener = changeListener != null ? changeListener : stack -> {
        };
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
     * A slot group determines whether items can be shift clicked here and if this slot is part of a group
     * which can be sorted with Inventory BogoSorter mod.
     *
     * @param slotGroup slot group id
     */
    public ModularSlot slotGroup(String slotGroup) {
        this.slotGroup = slotGroup;
        return this;
    }
}
