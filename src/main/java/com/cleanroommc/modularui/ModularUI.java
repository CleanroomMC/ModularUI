package com.cleanroommc.modularui;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mariuszgromada.math.mxparser.License;

@Mod(modid = ModularUI.ID,
        name = ModularUI.NAME,
        version = ModularUI.VERSION,
        dependencies = "required-after:mixinbooter@[8.0,);" +
                "after:bogorter@[1.4.0,);")
public class ModularUI {

    public static final String ID = MuiTags.MODID;
    public static final String NAME = "Modular UI";
    public static final String VERSION = MuiTags.VERSION;
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final String BOGO_SORT = "bogosorter";

    @SidedProxy(
            modId = ID,
            clientSide = "com.cleanroommc.modularui.ClientProxy",
            serverSide = "com.cleanroommc.modularui.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static ModularUI INSTANCE;

    private static boolean blurLoaded = false;
    private static boolean sorterLoaded = false;
    private static boolean jeiLoaded = false;

    static {
        // confirm mXparser license
        License.iConfirmNonCommercialUse("CleanroomMC");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        blurLoaded = Loader.isModLoaded("blur");
        sorterLoaded = Loader.isModLoaded(BOGO_SORT);
        jeiLoaded = Loader.isModLoaded("jei");
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        proxy.onServerLoad(event);
    }

    public static boolean isBlurLoaded() {
        return blurLoaded;
    }

    public static boolean isSortModLoaded() {
        return sorterLoaded;
    }

    public static boolean isJeiLoaded() {
        return jeiLoaded;
    }
}
