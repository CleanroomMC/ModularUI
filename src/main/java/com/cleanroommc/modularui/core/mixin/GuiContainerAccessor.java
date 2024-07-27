package com.cleanroommc.modularui.core.mixin;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(GuiContainer.class)
public interface GuiContainerAccessor {

    @Accessor
    void setXSize(int v);

    @Accessor
    void setYSize(int v);

    @Accessor
    void setGuiLeft(int v);

    @Accessor
    void setGuiTop(int v);

    @Accessor
    void setHoveredSlot(Slot slot);

    @Accessor
    Slot getHoveredSlot();

    @Accessor
    Slot getClickedSlot();

    @Accessor
    ItemStack getDraggedStack();

    @Accessor
    boolean getIsRightMouseClick();

    @Accessor
    boolean getDragSplitting();

    @Accessor
    Set<Slot> getDragSplittingSlots();

    @Accessor
    int getDragSplittingLimit();

    @Invoker
    void invokeUpdateDragSplitting();

    @Accessor
    int getDragSplittingRemnant();

    @Accessor
    ItemStack getReturningStack();

    @Accessor
    void setReturningStack(ItemStack stack);

    @Accessor
    Slot getReturningStackDestSlot();

    @Accessor
    int getTouchUpX();

    @Accessor
    int getTouchUpY();

    @Accessor
    long getReturningStackTime();

    @Invoker
    void invokeDrawGuiContainerForegroundLayer(int mouseX, int mouseY);

    @Invoker
    void invokeDrawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY);
}
