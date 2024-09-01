package com.cleanroommc.modularui.core.mixin;

import com.cleanroommc.modularui.overlay.OverlayStack;

import net.minecraft.client.gui.GuiButton;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * This mixin fixes some visual bugs that can happen with overlays.
 */
@Mixin(GuiButton.class)
public abstract class GuiButtonMixin {

    @Shadow protected boolean hovered;

    @Shadow
    protected abstract int getHoverState(boolean mouseOver);

    @Redirect(method = "drawButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiButton;getHoverState(Z)I"))
    public int draw(GuiButton instance, boolean mouseOver) {
        // fixes buttons being hovered when an overlay element is already hovered
        if (this.hovered) this.hovered = !OverlayStack.isHoveringOverlay();
        return getHoverState(this.hovered);
    }
}
