package com.cleanroommc.modularui.core.mixins.late.jei;

import com.cleanroommc.modularui.api.IMuiScreen;

import net.minecraftforge.client.event.GuiScreenEvent;

import mezz.jei.gui.GuiEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevent JEI to draw any tooltips when the cursor has a mui draggable element.
 */
@Mixin(value = GuiEventHandler.class, remap = false)
public class GuiEventHandlerMixin {

    @Inject(method = "onDrawScreenEventPost", at = @At(value = "INVOKE", target = "Lmezz/jei/gui/overlay/IngredientListOverlay;drawTooltips(Lnet/minecraft/client/Minecraft;II)V"), cancellable = true)
    public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event, CallbackInfo ci) {
        if (event.getGui() instanceof IMuiScreen muiScreen && muiScreen.getScreen().getContext().hasDraggable()) {
            ci.cancel(); // TODO fix JEI ghost dragging ingredient z layer
        }
    }
}
