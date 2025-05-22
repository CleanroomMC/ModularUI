package com.cleanroommc.modularui.core.mixin;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiUtils.class, remap = false)
public class GuiUtilsMixin {

    @Inject(method = "drawHoveringText(Lnet/minecraft/item/ItemStack;Ljava/util/List;IIIIILnet/minecraft/client/gui/FontRenderer;)V",
            at = @At("HEAD"), cancellable = true)
    private static void postRichTooltipEvent(ItemStack stack, List<String> textLines, int x, int y, int w, int h, int maxTextWidth, FontRenderer font, CallbackInfo ci) {
        if (ModularUIConfig.replaceVanillaTooltips && !textLines.isEmpty()) {
            RichTooltip.injectRichTooltip(stack, textLines, x, y);
            // Canceling vanilla tooltip rendering
            ci.cancel();
        }
    }
}
