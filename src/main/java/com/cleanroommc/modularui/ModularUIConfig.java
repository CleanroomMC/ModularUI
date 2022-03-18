package com.cleanroommc.modularui;

import net.minecraftforge.common.config.Config;

@Config(modid = ModularUI.ID)
public class ModularUIConfig {

    @Config.Name("Smooth progress bars")
    public static boolean smoothProgressbar = true;

    @Config.Comment("Open/Close animations can be combined")
    public static final Animations animations = new Animations();

    public static class Animations {
        public boolean openCloseFade = false;
        public boolean openCloseScale = true;
        public boolean openCloseTranslateFromBottom = true;
        public boolean openCloseRotateFast = false;
    }
}
