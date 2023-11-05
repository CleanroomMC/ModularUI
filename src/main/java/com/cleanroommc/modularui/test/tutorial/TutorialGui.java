package com.cleanroommc.modularui.test.tutorial;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.manager.ClientGUI;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class TutorialGui {

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityPlayer().getEntityWorld().isRemote && event.getItemStack().getItem() == Items.DIAMOND) {
            ClientGUI.open(createGui());
        }
    }

    public static ModularScreen createGui() {
        ModularPanel panel = ModularPanel.defaultPanel("tutorial_panel");
        panel.child(IKey.str("My first screen").asWidget()
                        .top(7).left(7))
                .child(new ButtonWidget<>()
                        .align(Alignment.Center)
                        .size(60, 16)
                        .overlay(IKey.str("Say Hello"))
                        .onMousePressed(button -> {
                            EntityPlayer player = Minecraft.getMinecraft().player;
                            player.sendMessage(new TextComponentString("Hello " + player.getName()));
                            return true;
                        }));
        return new ModularScreen(panel);
    }
}
