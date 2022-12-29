package com.cleanroommc.modularui;

import com.cleanroommc.modularui.screen.ModularScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onScroll(GuiScreenEvent.MouseInputEvent.Pre event) {
        ModularScreen screen = ModularScreen.getCurrent();
        if (screen != null) {
            int w = Mouse.getEventDWheel();
            if (w != 0) {
                ModularUI.LOGGER.info("Scrolling: {}", w);
            }
            if (w != 0 && screen.onMouseScroll(w > 0 ? ModularScreen.UpOrDown.UP : ModularScreen.UpOrDown.DOWN, Math.abs(w))) {
                event.setCanceled(true);
            }
        }
    }
}
