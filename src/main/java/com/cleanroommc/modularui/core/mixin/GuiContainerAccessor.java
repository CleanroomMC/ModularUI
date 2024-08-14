package com.cleanroommc.modularui.core.mixin;


import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface GuiContainerAccessor {

    @Accessor
    void setHoveredSlot(Slot slot);

    @Accessor
    Slot getHoveredSlot();

    @Accessor
    Slot getClickedSlot();

    @Accessor
    ItemStack getDraggingItem();

    @Accessor
    boolean getIsSplittingStack();

    @Accessor
    int getQuickCraftingType();

    @Invoker
    void invokeRecalculateQuickCraftRemaining();

    @Accessor
    int getQuickCraftingRemainder();

    @Accessor
    ItemStack getSnapbackItem();

    @Accessor
    void setSnapbackItem(ItemStack stack);

    @Accessor
    Slot getSnapbackEnd();

    @Accessor
    int getSnapbackStartX();

    @Accessor
    int getSnapbackStartY();

    @Accessor
    long getSnapbackTime();
}
