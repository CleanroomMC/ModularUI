package com.cleanroommc.modularui.core.mixin;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.RichTooltipEvent;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.sizer.Area;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
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

            RichTooltip tooltip = new RichTooltip(Area.ZERO);
            // Other positions don't really work due to the lack of GuiContext in non-modular uis
            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            textLines.forEach(line -> tooltip.addLine(IKey.str(line)));

            // Post an event from ppl
            RichTooltipEvent event = new RichTooltipEvent(stack, tooltip);
            if (MinecraftForge.EVENT_BUS.post(event)) return; // Event canceled, returning

            tooltip.draw(GuiContext.getDefault(), stack);
            // Canceling vanilla tooltip rendering
            ci.cancel();
        }
    }
}
