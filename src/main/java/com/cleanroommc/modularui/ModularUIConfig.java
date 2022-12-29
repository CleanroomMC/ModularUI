package com.cleanroommc.modularui;

import com.cleanroommc.modularui.screen.Tooltip;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

public class ModularUIConfig {

    public static boolean smoothProgressBar = true;

    // Tooltip
    public static boolean placeNextToPanelByDefault = true;
    // Default direction
    public static Tooltip.Pos tooltipPos = Tooltip.Pos.VERTICAL;

    public static boolean guiDebugMode = FMLLaunchHandler.isDeobfuscatedEnvironment();

    public static boolean placeTooltipNextToPanel() {
        return placeNextToPanelByDefault && Minecraft.getMinecraft().gameSettings.guiScale > 0;
    }
}
