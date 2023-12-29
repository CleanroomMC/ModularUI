package com.cleanroommc.modularui.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemStackItemHandler extends ItemStackHandler implements ICapabilityProvider {

    private static final String KEY_ITEMS = "Items";

    private final ItemStack container;

    public ItemStackItemHandler(ItemStack container, int slots) {
        super(slots);
        this.container = container;
    }

    protected void onContentsChanged(int slot) {
        var stack = this.stacks.get(slot);
        var writeTag = stack.isEmpty() ? new NBTTagCompound() : stack.serializeNBT();
        getItemsNbt().set(slot, writeTag);
    }

    public NBTTagList getItemsNbt() {
        NBTTagCompound nbt = this.container.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            this.container.setTagCompound(nbt);
        }
        if (!nbt.hasKey(KEY_ITEMS)) {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < getSlots(); i++) {
                list.appendTag(new NBTTagCompound());
            }
            nbt.setTag(KEY_ITEMS, list);
        }
        return nbt.getTagList(KEY_ITEMS, Constants.NBT.TAG_COMPOUND);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        var stack = super.extractItem(slot, amount, simulate);
        onContentsChanged(slot);
        return stack;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        for (int i = 0; i < this.stacks.size(); i++) {
            stacks.set(i, new ItemStack(getItemsNbt().getCompoundTagAt(i)));
        }
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this) : null;
    }
}
