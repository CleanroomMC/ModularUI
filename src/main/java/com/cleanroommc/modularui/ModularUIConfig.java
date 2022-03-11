package com.cleanroommc.modularui;

import net.minecraftforge.common.config.Config;

@Config(modid = ModularUI.ID)
public class ModularUIConfig {

    @Config.Name("Smooth progress bars")
    public static boolean smoothProgressbar = true;
}
