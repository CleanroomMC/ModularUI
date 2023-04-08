package com.cleanroommc.modularui;

import com.cleanroommc.modularui.screen.Tooltip;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ModularUIConfig {

    public static Configuration config;

    public static int defaultScrollSpeed = 30;
    public static boolean smoothProgressBar = true;
    public static int panelOpenCloseAnimationTime = 0;
    public static boolean placeTooltipNextToPanelByDefault = true;
    public static Tooltip.Pos tooltipPos = Tooltip.Pos.VERTICAL;
    public static boolean useDarkThemeByDefault = false;

    public static boolean guiDebugMode = ModularUI.isDevEnv;

    public static final String CATEGORY_RENDERING = "rendering";
    public static final String CATEGORY_DEBUG = "debug";

    private static final String LANG_PREFIX = Tags.MODID + ".config.";

    public static final String[] CATEGORIES = new String[] {
        CATEGORY_RENDERING,
        CATEGORY_DEBUG,
    };

    public static void init(File configFile) {
        config = new Configuration(configFile);
        syncConfig();
    }

    public static void syncConfig() {
        config.setCategoryComment(CATEGORY_RENDERING, "Rendering");
        config.setCategoryLanguageKey(CATEGORY_RENDERING, LANG_PREFIX + CATEGORY_RENDERING);
        config.setCategoryComment(CATEGORY_DEBUG, "Debug");
        config.setCategoryLanguageKey(CATEGORY_DEBUG, LANG_PREFIX + CATEGORY_DEBUG);

        // === Rendering ===

        defaultScrollSpeed = config.get(
            CATEGORY_RENDERING,
            "defaultScrollSpeed",
            30,
            "Amount of pixels scrolled.",
            1,
            100
        )
            .setLanguageKey(LANG_PREFIX + CATEGORY_RENDERING + ".defaultScrollSpeed")
            .getInt();

        smoothProgressBar = config.get(
            CATEGORY_RENDERING,
            "smoothProgressBar",
            true,
            "If progress bar should step in texture pixels or screen pixels. (Screen pixels are way smaller and therefore smoother)"
        )
            .setLanguageKey(LANG_PREFIX + CATEGORY_RENDERING + ".smoothProgressBar")
            .getBoolean();

        panelOpenCloseAnimationTime = config.get(
            CATEGORY_RENDERING,
            "panelOpenCloseAnimationTime",
            0,
            "Time in 1/60 sec to open and close panels."
        )
            .setLanguageKey(LANG_PREFIX + CATEGORY_RENDERING + ".panelOpenCloseAnimationTime")
            .getInt();

        placeTooltipNextToPanelByDefault = config.get(
            CATEGORY_RENDERING,
            "placeTooltipNextToPanelByDefault",
            true,
            "If tooltips should be placed next to a widget's panel or the widget by default."
        )
            .setLanguageKey(LANG_PREFIX + CATEGORY_RENDERING + ".placeTooltipNextToPanelByDefault")
            .getBoolean();

        tooltipPos = Tooltip.Pos.fromString(
            config.get(
                CATEGORY_RENDERING,
                "tooltipPos",
                "VERTICAL",
                "Default tooltip position around the widget or its panel. Select: ABOVE, BELOW, LEFT, RIGHT, VERTICAL, HORIZONTAL, NEXT_TO_MOUSE",
                new String[] {
                    "ABOVE",
                    "BELOW",
                    "LEFT",
                    "RIGHT",
                    "VERTICAL",
                    "HORIZONTAL",
                    "NEXT_TO_MOUSE",
                }
            )
                .setLanguageKey(LANG_PREFIX + CATEGORY_RENDERING + ".tooltipPos")
                .getString());

        useDarkThemeByDefault = config.get(
            CATEGORY_RENDERING,
            "useDarkThemeByDefault",
            false,
            "If true and not specified otherwise, screens will try to use the 'vanilla_dark' theme."
        )
            .setLanguageKey(LANG_PREFIX + CATEGORY_RENDERING + ".useDarkThemeByDefault")
            .getBoolean();

        // === Debug ===

        guiDebugMode = config.get(
            CATEGORY_DEBUG,
            "guiDebugMode",
            ModularUI.isDevEnv,
            "If true, widget outlines and widget information will be drawn."
        )
            .setLanguageKey(LANG_PREFIX + CATEGORY_DEBUG + ".guiDebugMode")
            .getBoolean();
    }

    public static boolean placeTooltipNextToPanel() {
        return placeTooltipNextToPanelByDefault && Minecraft.getMinecraft().gameSettings.guiScale > 0;
    }
}
