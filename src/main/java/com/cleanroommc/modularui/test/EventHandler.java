package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.holoui.HoloScreenEntity;
import com.cleanroommc.modularui.holoui.HoloUI;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityPlayer().getEntityWorld().isRemote && event.getItemStack().getItem() == Items.DIAMOND) {
            //GuiManager.openClientUI(Minecraft.getMinecraft().player, new TestGui());
            HoloUI.builder()
                    .inFrontOf(Minecraft.getMinecraft().player, 5, false)
                    .open(new TestGui());
        }
    }
}
