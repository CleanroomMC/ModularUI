package com.cleanroommc.modularui;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID, name = Tags.MODNAME, version = Tags.VERSION, dependencies = ModularUI.DEPENDENCIES, guiFactory = ModularUI.GUI_FACTORY)
public class ModularUI {

    public static final String DEPENDENCIES = "required-after:gtnhmixins@[2.0.1,); "
        + "required-after:NotEnoughItems@[2.3.27-GTNH,);"
        + "after:hodgepodge@[2.0.0,);"
        + "before:gregtech";
    public static final String GUI_FACTORY = Tags.GROUPNAME + ".config.GuiFactory";

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    public static final String MODID_GT5U = "gregtech";
    public static final String MODID_GT6 = "gregapi_post";
    public static final boolean isGT5ULoaded = Loader.isModLoaded(MODID_GT5U) && !Loader.isModLoaded(MODID_GT6);
    public static final boolean isHodgepodgeLoaded = Loader.isModLoaded("hodgepodge");

    @SidedProxy(
        modId = Tags.MODID,
        clientSide = Tags.GROUPNAME + ".ClientProxy",
        serverSide = Tags.GROUPNAME + ".CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static ModularUI INSTANCE;

    @SideOnly(Side.CLIENT)
    static Timer timer60Fps;

    public static final boolean isDevEnv = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @SideOnly(Side.CLIENT)
    public static Minecraft getMC() {
        return Minecraft.getMinecraft();
    }

    @SideOnly(Side.CLIENT)
    public static Timer getTimer60Fps() {
        return timer60Fps;
    }
}
