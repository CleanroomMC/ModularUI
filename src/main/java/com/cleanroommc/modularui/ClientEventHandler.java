package com.cleanroommc.modularui;

import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    private static long ticks = 0L;

    public static long getTicks() {
        return ticks;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ticks++;
        }
    }

    @SubscribeEvent
    public static void preDraw(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
        }
        Stencil.reset();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGuiInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (hasDraggable(event)) {
            // cancel interactions with other mods
            try {
                event.getGui().handleMouseInput();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGuiInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (hasDraggable(event)) {
            // cancel interactions with other mods
            try {
                event.getGui().handleKeyboardInput();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            event.setCanceled(true);
        }
    }

    private static boolean hasDraggable(GuiScreenEvent event) {
        return event.getGui() instanceof GuiContainerWrapper screenWrapper && screenWrapper.getScreen().getContext().hasDraggable();
    }
}
