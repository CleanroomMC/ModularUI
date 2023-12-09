package com.cleanroommc.modularui;

import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.factory.ClientGUI;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    private static long ticks = 0L;

    public static long getTicks() {
        return ticks;
    }

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
            ticks++;
            GuiManager.checkQueuedScreen();
        }
    }

    @SubscribeEvent
    public static void onOpenScreen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiScreenWrapper && Minecraft.getMinecraft().currentScreen != null) {
            // another screen is already open, don't fade in the dark background as it's already there
            ((GuiScreenWrapper) event.getGui()).setDoAnimateTransition(false);
        }
        if (!GuiManager.isOpeningQueue() && Minecraft.getMinecraft().currentScreen instanceof GuiScreenWrapper) {
            // opening a screen while a modular screen is open can cause crashes
            // queue the screen to open it on next tick
            if (event.getGui() != null) {
                ClientGUI.open(event.getGui());
            } else {
                ClientGUI.close();
            }
            event.setCanceled(true);
        }
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
        return event.getGui() instanceof GuiScreenWrapper && ((GuiScreenWrapper) event.getGui()).getScreen().getContext().hasDraggable();
    }
}
