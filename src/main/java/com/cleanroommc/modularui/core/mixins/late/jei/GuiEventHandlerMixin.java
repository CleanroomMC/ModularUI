package com.cleanroommc.modularui.core.mixins.late.jei;

import com.cleanroommc.modularui.api.IMuiScreen;

import mezz.jei.gui.GuiEventHandler;

import mezz.jei.gui.GuiScreenHelper;

import net.minecraftforge.client.event.GuiScreenEvent;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Modifies JEI to not draw tooltips when the cursor has a mui draggable element or the mouse is inside an exclusion area.
 */
@Mixin(value = GuiEventHandler.class, remap = false)
public class GuiEventHandlerMixin {

    @Shadow
    @Final
    private GuiScreenHelper guiScreenHelper;

    @Inject(method = "onDrawScreenEventPost", at = @At(value = "INVOKE", target = "Lmezz/jei/gui/overlay/IngredientListOverlay;drawTooltips(Lnet/minecraft/client/Minecraft;II)V"), cancellable = true)
    public void onDrawScreenEventPost(GuiScreenEvent.DrawScreenEvent.Post event, CallbackInfo ci) {
        if (event.getGui() instanceof IMuiScreen muiScreen && muiScreen.getScreen().getContext().hasDraggable()) {
            ci.cancel();
            return;
        }
        if (guiScreenHelper.isInGuiExclusionArea(event.getMouseX(), event.getMouseY())) {
            ci.cancel();
        }
    }
}
