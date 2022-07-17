package com.cleanroommc.modularui;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

@Config(modid = ModularUI.ID)
public class ModularUIConfig {

    @Config.Name("Enable Debug information")
    public static boolean debug = FMLLaunchHandler.isDeobfuscatedEnvironment();

    @Config.Name("Smooth progress bars")
    public static boolean smoothProgressbar = true;

    @Config.Comment("Open/Close animations can be combined")
    public static final Animations animations = new Animations();

    public static class Animations {
        @Config.RangeInt(min = 0, max = 3000)
        public int openCloseDurationMs = 250;
        public boolean openCloseFade = false;
        public boolean openCloseScale = true;
        public boolean openCloseTranslateFromBottom = true;
        public boolean openCloseRotateFast = false;
    }
}
