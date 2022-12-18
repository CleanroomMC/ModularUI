package com.cleanroommc.modularui;

import com.cleanroommc.modularui.manager.GuiManager;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.test.EventHandler;
import com.cleanroommc.modularui.test.TestBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModularUI.ID, name = ModularUI.NAME, version = ModularUI.VERSION, dependencies = "required-after:mixinbooter@[4.2,);")
public class ModularUI {

    public static final String ID = "modularui";
    public static final String NAME = "Modular UI";
    public static final String VERSION = "2.0.0";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final String BOGO_SORT = "bogosorter";

    @Mod.Instance
    public static ModularUI INSTANCE;

    //@SidedProxy(modId = ID, clientSide = "com.cleanroommc.modularui.ClientProxy", serverSide = "com.cleanroommc.modularui.CommonProxy")
    //public static CommonProxy proxy;

    public static final Timer TIMER_60_FPS = new Timer(60f);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(ID, GuiManager.INSTANCE);

        //proxy.preInit(event);
        MinecraftForge.EVENT_BUS.register(TestBlock.class);

        if (FMLLaunchHandler.isDeobfuscatedEnvironment()) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
            TestBlock.preInit();
        }

        NetworkHandler.init();
        //TODO UIInfos.init();
        //WidgetJsonRegistry.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        //proxy.init();
        if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
            //JsonLoader.loadJson();
        }
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        //proxy.postInit();
    }

    public static boolean isSortModLoaded() {
        return Loader.isModLoaded(BOGO_SORT);
    }

    public static Minecraft getMC() {
        return Minecraft.getMinecraft();
    }


}
