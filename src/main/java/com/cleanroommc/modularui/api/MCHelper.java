package com.cleanroommc.modularui.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;

public class MCHelper {

    public static boolean hasMc() {
        return getMc() != null;
    }

    public static Minecraft getMc() {
        return Minecraft.getMinecraft();
    }

    public static EntityPlayerSP getPlayer() {
        if (hasMc()) {
            return getMc().player;
        }
        return null;
    }

    public static boolean closeScreen() {
        if (!hasMc()) return false;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null) {
            player.closeScreen();
            return true;
        }
        Minecraft.getMinecraft().displayGuiScreen(null);
        return false;
    }

    public static boolean displayScreen(GuiScreen screen) {
        Minecraft mc = getMc();
        if (mc != null) {
            mc.displayGuiScreen(screen);
            return true;
        }
        return false;
    }

    public static GuiScreen getCurrentScreen() {
        Minecraft mc = getMc();
        return mc != null ? mc.currentScreen : null;
    }
}
