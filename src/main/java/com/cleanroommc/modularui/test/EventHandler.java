package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.manager.GuiManager;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ModularUI.ID)
public class EventHandler {

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityPlayer().getEntityWorld().isRemote && event.getItemStack().getItem() == Items.DIAMOND) {
            GuiManager.openClientUI(Minecraft.getMinecraft().player, new TestGui());
        }
    }
}
