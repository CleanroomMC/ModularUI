package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.screen.JeiSettingsImpl;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

/**
 * Helper class to open client only GUIs. This class is safe to use inside a Modular GUI.
 * Direct calls to {@link net.minecraft.client.Minecraft#displayGuiScreen(GuiScreen)} are redirected to this class if
 * the current screen is a Modular GUI.
 */
@SideOnly(Side.CLIENT)
public class ClientGUI {

    private ClientGUI() {
    }

    /**
     * Opens a modular screen on the next client tick with default jei settings.
     *
     * @param screen new modular screen
     */
    public static void open(@NotNull ModularScreen screen) {
        open(screen, new JeiSettingsImpl());
    }

    /**
     * Opens a modular screen on the next client tick with custom jei settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen      new modular screen
     * @param jeiSettings custom jei settings
     */
    public static void open(@NotNull ModularScreen screen, @NotNull JeiSettingsImpl jeiSettings) {
        GuiManager.openScreen(screen, jeiSettings);
    }

    /**
     * Opens a {@link GuiScreen} on the next client tick.
     *
     * @param screen screen to open
     */
    public static void open(GuiScreen screen) {
        Minecraft.getMinecraft().displayGuiScreen(screen);
    }

    /**
     * Closes any GUI that is open in this tick.
     */
    public static void close() {
        Minecraft.getMinecraft().displayGuiScreen(null);
    }
}
