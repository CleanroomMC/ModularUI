package com.cleanroommc.modularui.core.mixin;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.screen.IClickableGuiContainer;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiContainer.class)
public class GuiContainerMixin implements IClickableGuiContainer {

    @Shadow
    private Slot hoveredSlot;

    @Unique
    private Slot modularUI$clickedSlot;

    /**
     * Mixin into ModularUI screen wrapper to return the true hovered slot.
     * The method is private and only the mouse pos is ever passed to this method.
     * That's why we can just return the current hovered slot.
     */
    @Inject(method = "getSlotAtPosition", at = @At("HEAD"), cancellable = true)
    public void getSlot(int x, int y, CallbackInfoReturnable<Slot> cir) {
        if (this.modularUI$clickedSlot != null) {
            cir.setReturnValue(this.modularUI$clickedSlot);
        } else if (IMuiScreen.class.isAssignableFrom(this.getClass())) {
            cir.setReturnValue(this.hoveredSlot);
        }
    }

    @Override
    public void modularUI$setClickedSlot(Slot slot) {
        this.modularUI$clickedSlot = slot;
    }

    @Override
    public Slot modularUI$getClickedSlot() {
        return modularUI$clickedSlot;
    }
}
