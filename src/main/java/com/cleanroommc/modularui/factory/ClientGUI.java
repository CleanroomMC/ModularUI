package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.screen.JeiSettingsImpl;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

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
        open(screen, new UISettings());
    }

    /**
     * Opens a modular screen on the next client tick with custom jei settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen      new modular screen
     * @param jeiSettings custom jei settings
     */
    public static void open(@NotNull ModularScreen screen, @NotNull JeiSettingsImpl jeiSettings) {
        GuiManager.openScreen(screen, new UISettings(jeiSettings));
    }

    /**
     * Opens a modular screen on the next client tick with custom jei settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen    new modular screen
     * @param container custom container
     */
    public static void open(@NotNull ModularScreen screen, @Nullable Supplier<ModularContainer> container) {
        UISettings settings = new UISettings();
        settings.customContainer(container);
        GuiManager.openScreen(screen, settings);
    }

    /**
     * Opens a modular screen on the next client tick with custom jei settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen      new modular screen
     * @param jeiSettings custom jei settings
     * @param container   custom container
     */
    public static void open(@NotNull ModularScreen screen, @NotNull JeiSettingsImpl jeiSettings, @Nullable Supplier<ModularContainer> container) {
        UISettings settings = new UISettings(jeiSettings);
        settings.customContainer(container);
        GuiManager.openScreen(screen, settings);
    }

    /**
     * Opens a modular screen on the next client tick with custom jei settings.
     * It needs to be opened in next tick, because we might break the current GUI if we open it now.
     *
     * @param screen   new modular screen
     * @param settings ui settings
     */
    public static void open(@NotNull ModularScreen screen, @NotNull UISettings settings) {
        GuiManager.openScreen(screen, settings);
    }

    /**
     * Opens a {@link GuiScreen} on the next client tick.
     *
     * @param screen screen to open
     */
    public static void open(GuiScreen screen) {
        MCHelper.displayScreen(screen);
    }

    /**
     * Closes any GUI that is open in this tick.
     */
    public static void close() {
        MCHelper.displayScreen(null);
    }
}
