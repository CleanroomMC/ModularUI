package com.cleanroommc.modularui.manager;

import com.cleanroommc.modularui.screen.JeiSettings;
import com.cleanroommc.modularui.screen.ModularScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class to open client only GUIs.
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
        open(screen, new JeiSettings());
    }

    /**
     * Opens a modular screen on the next client tick with custom jei settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen      new modular screen
     * @param jeiSettings custom jei settings
     */
    public static void open(@NotNull ModularScreen screen, @NotNull JeiSettings jeiSettings) {
        GuiManager.queuedClientScreen = screen;
        GuiManager.queuedJeiSettings = jeiSettings;
    }
}
