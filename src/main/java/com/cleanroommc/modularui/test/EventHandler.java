package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.ClientGUI;

import com.cleanroommc.modularui.screen.RichTooltipEvent;

import net.minecraft.init.Items;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityPlayer().getEntityWorld().isRemote && event.getItemStack().getItem() == Items.DIAMOND) {
            //GuiManager.openClientUI(Minecraft.getMinecraft().player, new TestGui());
            /*HoloUI.builder()
                    .inFrontOf(Minecraft.getMinecraft().player, 5, false)
                    .screenScale(0.5f)
                    .open(new TestGui());*/
            //ClientGUI.open(new ResizerTest());
            ClientGUI.open(new TestGui());
        }
    }

    @SubscribeEvent
    public static void onRichTooltip(RichTooltipEvent.Pre event) {
        event.getTooltip()
                .add(IKey.str("Powered By: ").style(TextFormatting.GOLD, TextFormatting.ITALIC))
                .add(GuiTextures.MUI_LOGO.asIcon().size(18)).newLine();
    }
}
