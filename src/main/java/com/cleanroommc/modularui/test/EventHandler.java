package com.cleanroommc.modularui.test;

import net.minecraft.init.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityPlayer().getEntityWorld().isRemote && event.getItemStack().getItem() == Items.DIAMOND) {
            //GuiManager.openClientUI(Minecraft.getMinecraft().player, new TestGui());
            ModularScreenEntity modularScreenEntity = new ModularScreenEntity(event.getEntityPlayer().world);
            modularScreenEntity.setScreen(new TestGui());
            modularScreenEntity.setPosition(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ);
            modularScreenEntity.spawnInWorld();
        }
    }
}
