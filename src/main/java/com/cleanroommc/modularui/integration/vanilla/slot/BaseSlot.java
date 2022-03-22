package com.cleanroommc.modularui.integration.vanilla.slot;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class BaseSlot extends SlotItemHandler {

    protected final boolean output;
    protected final boolean phantom;

    protected boolean enabled = true;
    // lower priority means it gets targeted first
    // hotbar 20, player inventory 40, machine input 0
    private int shiftClickPriority = 0;
    private Runnable changeListener;
    private Predicate<ItemStack> filter;

    public static BaseSlot phantom() {
        return phantom(new ItemStackHandler(), 0, false);
    }

    public static BaseSlot phantom(IItemHandlerModifiable handler, int index, boolean output) {
        return new BaseSlot(handler, index, output, true);
    }

    public BaseSlot(IItemHandlerModifiable inventory, int index) {
        this(inventory, index, false, false);
    }

    public BaseSlot(IItemHandlerModifiable inventory, int index, boolean output, boolean phantom) {
        super(inventory, index, 0, 0);
        this.output = output;
        this.phantom = phantom;
        if (inventory instanceof PlayerMainInvWrapper) {
            setShiftClickPriority(index > 8 ? 40 : 20);
        }
    }

    public BaseSlot setShiftClickPriority(int shiftClickPriority) {
        this.shiftClickPriority = shiftClickPriority;
        return this;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !this.phantom && isItemValidPhantom(stack);
    }

    public boolean isItemValidPhantom(ItemStack stack) {
        return !this.output && (filter == null || filter.test(stack)) && getItemHandler().isItemValid(getSlotIndex(), stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return !this.phantom && super.canTakeStack(playerIn);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isOutput() {
        return output;
    }

    public boolean isPhantom() {
        return phantom;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getShiftClickPriority() {
        return shiftClickPriority;
    }

    public void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public void setFilter(Predicate<ItemStack> filter) {
        this.filter = filter;
    }

    @Override
    public void onSlotChanged() {
        if (this.changeListener != null) {
            this.changeListener.run();
        }
    }

    // handle background by widgets
    @Override
    public ResourceLocation getBackgroundLocation() {
        return null;
    }

    @Nullable
    @Override
    public String getSlotTexture() {
        return null;
    }

    @Nullable
    @Override
    public TextureAtlasSprite getBackgroundSprite() {
        return null;
    }

    public void incrementStackCount(int amount) {
        ItemStack stack = getStack();
        if (stack.isEmpty()) {
            return;
        }
        if (amount < 0) {
            amount = Math.max(0, stack.getCount() + amount);
        } else {
            amount = Math.min(stack.getCount() + amount, getItemStackLimit(stack));
        }
        stack.setCount(amount);
    }
}
