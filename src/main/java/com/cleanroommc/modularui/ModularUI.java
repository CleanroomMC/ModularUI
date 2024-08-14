package com.cleanroommc.modularui;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ModularUI.MOD_ID)
public class ModularUI {

    public static final String MOD_ID = "modularui";
    public static final String NAME = "Modular UI";
    public static final String VERSION = "2.5.0-rc1";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static final String BOGO_SORT = "bogosorter";



    public static ModularUI INSTANCE;

    private static boolean blurLoaded = false;
    private static boolean sorterLoaded = false;

    public ModularUI() {
        ModularUI.init();
        DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }

    public static void init() {
        LOGGER.info("{} is initializing on platform: {}", NAME, "forge");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    /*public static boolean isBlurLoaded() {
        return blurLoaded;
    }

    public static boolean isSortModLoaded() {
        return sorterLoaded;
    }*/
}
