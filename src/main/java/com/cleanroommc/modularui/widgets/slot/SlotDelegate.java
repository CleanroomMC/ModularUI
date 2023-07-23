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
        this.slot.onSlotChange(p_75220_1_, p_75220_2_);
    }

    @Override
    protected void onCrafting(@NotNull ItemStack stack, int amount) {
        ((SlotAccessor) this.slot).invokeOnCrafting(stack, amount);
    }

    @Override
    protected void onSwapCraft(int p_190900_1_) {
        ((SlotAccessor) this.slot).invokeOnSwapCraft(p_190900_1_);
    }

    @Override
    protected void onCrafting(@NotNull ItemStack stack) {
        ((SlotAccessor) this.slot).invokeOnCrafting(stack);
    }

    @Override
    public @NotNull ItemStack onTake(@NotNull EntityPlayer thePlayer, @NotNull ItemStack stack) {
        return this.slot.onTake(thePlayer, stack);
    }

    @Override
    public boolean isItemValid(@NotNull ItemStack stack) {
        return this.slot.isItemValid(stack);
    }

    @Override
    public @NotNull ItemStack getStack() {
        return this.slot.getStack();
    }

    @Override
    public boolean getHasStack() {
        return this.slot.getHasStack();
    }

    @Override
    public void putStack(@NotNull ItemStack stack) {
        this.slot.putStack(stack);
    }

    @Override
    public void onSlotChanged() {
        this.slot.onSlotChanged();
    }

    @Override
    public int getSlotStackLimit() {
        return this.slot.getSlotStackLimit();
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return this.slot.getItemStackLimit(stack);
    }

    @Nullable
    @Override
    public String getSlotTexture() {
        return this.slot.getSlotTexture();
    }

    @Override
    public @NotNull ItemStack decrStackSize(int amount) {
        return this.slot.decrStackSize(amount);
    }

    @Override
    public boolean isHere(@NotNull IInventory inv, int slotIn) {
        return this.slot.isHere(inv, slotIn);
    }

    @Override
    public boolean canTakeStack(@NotNull EntityPlayer playerIn) {
        return this.slot.canTakeStack(playerIn);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled && this.slot.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public @NotNull ResourceLocation getBackgroundLocation() {
        return this.slot.getBackgroundLocation();
    }

    @Override
    public void setBackgroundLocation(@NotNull ResourceLocation texture) {
        this.slot.setBackgroundLocation(texture);
    }

    @Override
    public void setBackgroundName(@Nullable String name) {
        this.slot.setBackgroundName(name);
    }

    @Nullable
    @Override
    public TextureAtlasSprite getBackgroundSprite() {
        return this.slot.getBackgroundSprite();
    }

    @Override
    protected @NotNull TextureMap getBackgroundMap() {
        return ((SlotAccessorClient) this.slot).invokeGetBackgroundMap();
    }

    @Override
    public int getSlotIndex() {
        return this.slot.getSlotIndex();
    }

    @Override
    public boolean isSameInventory(@NotNull Slot other) {
        return this.slot.isSameInventory(other);
    }

    public Slot getDelegate() {
        return this.slot;
    }
}
