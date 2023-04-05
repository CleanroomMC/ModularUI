package com.cleanroommc.modularui.widgets.slot;

import com.cleanroommc.modularui.api.SlotAccessor;
import com.cleanroommc.modularui.core.mixin.SlotAccessorClient;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SlotDelegate extends Slot implements ICustomSlot {

    private boolean enabled = true;

    public static SlotDelegate create(Slot slot) {
        return create(slot, false);
    }

    public static SlotDelegate create(Slot slot, boolean force) {
        Objects.requireNonNull(slot);
        if (force) {
            return new SlotDelegate(slot);
        }
        Slot delegated = slot;
        while (delegated instanceof SlotDelegate) {
            delegated = ((SlotDelegate) delegated).getDelegate();
        }
        SlotDelegate newDelegate = new SlotDelegate(delegated);
        newDelegate.xPos = slot.xPos;
        newDelegate.yPos = slot.yPos;
        return newDelegate;
    }

    private final Slot slot;

    private SlotDelegate(Slot slot) {
        super(slot.inventory, slot.slotNumber, slot.xPos, slot.yPos);
        this.slot = slot;
    }

    @Override
    public void onSlotChange(@NotNull ItemStack p_75220_1_, @NotNull ItemStack p_75220_2_) {
        slot.onSlotChange(p_75220_1_, p_75220_2_);
    }

    @Override
    protected void onCrafting(@NotNull ItemStack stack, int amount) {
        ((SlotAccessor) slot).invokeOnCrafting(stack, amount);
    }

    @Override
    protected void onSwapCraft(int p_190900_1_) {
        ((SlotAccessor) slot).invokeOnSwapCraft(p_190900_1_);
    }

    @Override
    protected void onCrafting(@NotNull ItemStack stack) {
        ((SlotAccessor) slot).invokeOnCrafting(stack);
    }

    @Override
    public @NotNull ItemStack onTake(@NotNull EntityPlayer thePlayer, @NotNull ItemStack stack) {
        return slot.onTake(thePlayer, stack);
    }

    @Override
    public boolean isItemValid(@NotNull ItemStack stack) {
        return slot.isItemValid(stack);
    }

    @Override
    public @NotNull ItemStack getStack() {
        return slot.getStack();
    }

    @Override
    public boolean getHasStack() {
        return slot.getHasStack();
    }

    @Override
    public void putStack(@NotNull ItemStack stack) {
        slot.putStack(stack);
    }

    @Override
    public void onSlotChanged() {
        slot.onSlotChanged();
    }

    @Override
    public int getSlotStackLimit() {
        return slot.getSlotStackLimit();
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return slot.getItemStackLimit(stack);
    }

    @Nullable
    @Override
    public String getSlotTexture() {
        return slot.getSlotTexture();
    }

    @Override
    public @NotNull ItemStack decrStackSize(int amount) {
        return slot.decrStackSize(amount);
    }

    @Override
    public boolean isHere(@NotNull IInventory inv, int slotIn) {
        return slot.isHere(inv, slotIn);
    }

    @Override
    public boolean canTakeStack(@NotNull EntityPlayer playerIn) {
        return slot.canTakeStack(playerIn);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled && slot.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public @NotNull ResourceLocation getBackgroundLocation() {
        return slot.getBackgroundLocation();
    }

    @Override
    public void setBackgroundLocation(@NotNull ResourceLocation texture) {
        slot.setBackgroundLocation(texture);
    }

    @Override
    public void setBackgroundName(@Nullable String name) {
        slot.setBackgroundName(name);
    }

    @Nullable
    @Override
    public TextureAtlasSprite getBackgroundSprite() {
        return slot.getBackgroundSprite();
    }

    @Override
    protected @NotNull TextureMap getBackgroundMap() {
        return ((SlotAccessorClient) slot).invokeGetBackgroundMap();
    }

    @Override
    public int getSlotIndex() {
        return slot.getSlotIndex();
    }

    @Override
    public boolean isSameInventory(@NotNull Slot other) {
        return slot.isSameInventory(other);
    }

    public Slot getDelegate() {
        return slot;
    }
}
