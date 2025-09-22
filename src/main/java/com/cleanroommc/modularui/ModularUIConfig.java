package com.cleanroommc.modularui;

import com.cleanroommc.modularui.screen.RichTooltip;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

@Config(modid = ModularUI.ID)
public class ModularUIConfig {

    @Config.Comment("Amount of pixels scrolled")
    @Config.RangeInt(min = 1, max = 100)
    public static int defaultScrollSpeed = 30;

    @Config.Comment("If progress bar should step in texture pixels or screen pixels. (Screen pixels are way smaller and therefore smoother)")
    public static boolean smoothProgressBar = true;

    // Default direction
    @Config.Comment("Default tooltip position around the widget or its panel.")
    public static RichTooltip.Pos tooltipPos = RichTooltip.Pos.VERTICAL;

    @Config.Comment("If true, pressing ESC key in the text field will restore the last text instead of confirming current one.")
    public static boolean escRestoreLastText = false;

    @Config.Comment("If true, widget outlines and widget information will be drawn.")
    public static boolean guiDebugMode = FMLLaunchHandler.isDeobfuscatedEnvironment();

    @Config.Comment("If true and not specified otherwise, screens will try to use the 'vanilla_dark' theme.")
    public static boolean useDarkThemeByDefault = false;

    @Config.RequiresMcRestart
    @Config.Comment("Enables a test block, test item with a test gui and opening a gui by right clicking a diamond.")
    public static boolean enableTestGuis = FMLLaunchHandler.isDeobfuscatedEnvironment();

    @Config.RequiresMcRestart
    @Config.Comment("Enables a test overlay shown on title screen and watermark shown on every GuiContainer.")
    public static boolean enableTestOverlays = false;

    @Config.Comment("If true, vanilla tooltip will be replaced with MUI's RichTooltip")
    public static boolean replaceVanillaTooltips = false;

    @Config.Comment({"The format prefix of the mod name tooltip line.", "Default (Blue and Italic): §9§o"})
    public static String modNameFormat = "§9§o";
}
