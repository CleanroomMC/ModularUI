package com.cleanroommc.modularui.common.internal.mixin;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiContainer.class)
public abstract class GuiContainerMixin implements GuiContainerAccess {

    @Shadow
    public Container inventorySlots;

    @Shadow
    protected abstract boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY);

    /**
     * @author brachy84
     * @reason because mojang dum dum
     */
    @Overwrite
    private Slot getSlotAtPosition(int x, int y) {
        return getSlotAt(x, y);
    }

    @Override
    public Slot getSlotAt(float x, float y) {
        for (int i = 0; i < inventorySlots.inventorySlots.size(); ++i) {
            Slot slot = this.inventorySlots.inventorySlots.get(i);

            if (this.isOverSlot(slot, x, y) && slot.isEnabled()) {
                return slot;
            }
        }
        return null;
    }

    @Override
    public boolean isOverSlot(Slot slot, float x, float y) {
        return isPointInRegion(slot.xPos, slot.yPos, 16, 16, (int) x, (int) y);
    }
}
