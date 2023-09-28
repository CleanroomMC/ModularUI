package com.cleanroommc.modularui;

import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.manager.GuiManager;
import com.cleanroommc.modularui.screen.ModularScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onScroll(GuiScreenEvent.MouseInputEvent.Pre event) {
        ModularScreen screen = ModularScreen.getCurrent();
        if (screen != null) {
            int w = Mouse.getEventDWheel();
            if (w != 0 && screen.onMouseScroll(w > 0 ? ModularScreen.UpOrDown.UP : ModularScreen.UpOrDown.DOWN, Math.abs(w))) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void preDraw(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
        }
        Stencil.reset();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GuiManager.checkQueuedScreen();
        }
    }
}
