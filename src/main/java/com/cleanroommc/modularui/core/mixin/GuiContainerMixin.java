package com.cleanroommc.modularui.core.mixin;

import com.cleanroommc.modularui.screen.GuiScreenWrapper;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiContainer.class)
public class GuiContainerMixin {

    @Shadow
    private Slot hoveredSlot;

    /**
     * Mixin into ModularUI screen wrapper to return the true hovered slot.
     * The method is private and only the mouse pos is ever passed to this method.
     * That's why we can just return the current hovered slot.
     */
    @Inject(method = "getSlotAtPosition", at = @At("HEAD"), cancellable = true)
    public void getSlot(int x, int y, CallbackInfoReturnable<Slot> cir) {
        if (((Object) this).getClass() == GuiScreenWrapper.class) {
            cir.setReturnValue(this.hoveredSlot);
        }
    }
}
