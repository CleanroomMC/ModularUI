package com.cleanroommc.modularui.common.internal.wrapper;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class BaseSlot extends SlotItemHandler {

    protected final boolean phantom;
    protected boolean canInsert = true, canTake = true;

    protected boolean enabled = true;
    // lower priority means it gets targeted first
    // hotbar 20, player inventory 40, machine input 0
    private int shiftClickPriority = 0;
    private boolean ignoreStackSizeLimit = false;
    private Runnable changeListener;
    private Predicate<ItemStack> filter;

    public static BaseSlot phantom() {
        return phantom(new ItemStackHandler(), 0);
    }

    public static BaseSlot phantom(IItemHandlerModifiable handler, int index) {
        return new BaseSlot(handler, index, true);
    }

    public BaseSlot(IItemHandlerModifiable inventory, int index) {
        this(inventory, index, false);
    }

    public BaseSlot(IItemHandlerModifiable inventory, int index, boolean phantom) {
        super(inventory, index, 0, 0);
        this.phantom = phantom;
        if (inventory instanceof PlayerMainInvWrapper) {
            setShiftClickPriority(index > 8 ? 40 : 20);
        }
        if (this.phantom) {
            this.shiftClickPriority += 10;
        }
    }

    public BaseSlot setShiftClickPriority(int shiftClickPriority) {
        this.shiftClickPriority = shiftClickPriority;
        return this;
    }

    public BaseSlot disableShiftInsert() {
        return setShiftClickPriority(Integer.MIN_VALUE);
    }

    public BaseSlot setAccess(boolean canInsert, boolean canTake) {
        this.canTake = canTake;
        this.canInsert = canInsert;
        return this;
    }

    public BaseSlot setIgnoreStackSizeLimit(boolean ignoreStackSizeLimit) {
        this.ignoreStackSizeLimit = ignoreStackSizeLimit;
        return this;
    }

    @Override
    public boolean isItemValid(@NotNull ItemStack stack) {
        return !this.phantom && isItemValidPhantom(stack);
    }

    public boolean isItemValidPhantom(ItemStack stack) {
        return this.canInsert && (filter == null || filter.test(stack)) && getItemHandler().isItemValid(getSlotIndex(), stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return !this.phantom && canTake && super.canTakeStack(playerIn);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isCanInsert() {
        return canInsert;
    }

    public boolean isPhantom() {
        return phantom;
    }

    public boolean isIgnoreStackSizeLimit() {
        return ignoreStackSizeLimit;
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
        super.onSlotChanged();
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
        int oldAmount = stack.getCount();
        if (amount < 0) {
            amount = Math.max(0, oldAmount + amount);
        } else {
            if (Integer.MAX_VALUE - amount < oldAmount) {
                amount = Integer.MAX_VALUE;
            } else {
                int maxSize = getItemHandler().getSlotLimit(getSlotIndex());
                if (!isIgnoreStackSizeLimit() && stack.getMaxStackSize() < maxSize) {
                    maxSize = stack.getMaxStackSize();
                }
                amount = Math.min(oldAmount + amount, maxSize);
            }
        }
        if (oldAmount != amount) {
            stack.setCount(amount);
            onSlotChanged();
        }
    }
}
