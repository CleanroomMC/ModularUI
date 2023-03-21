package com.cleanroommc.modularui;

import com.cleanroommc.modularui.keybind.KeyBindHandler;
import com.cleanroommc.modularui.manager.GuiInfos;
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
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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

    @SideOnly(Side.CLIENT)
    private static Timer timer60Fps;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(ID, GuiManager.INSTANCE);
        GuiInfos.init();

        if (FMLCommonHandler.instance().getSide().isClient()) {
            timer60Fps = new Timer(60f);
            MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
            MinecraftForge.EVENT_BUS.register(KeyBindHandler.class);

            if (FMLLaunchHandler.isDeobfuscatedEnvironment()) {
                MinecraftForge.EVENT_BUS.register(EventHandler.class);
            }
        }

        if (FMLLaunchHandler.isDeobfuscatedEnvironment()) {
            MinecraftForge.EVENT_BUS.register(TestBlock.class);
            TestBlock.preInit();
        }

        NetworkHandler.init();
    }

    public static boolean isSortModLoaded() {
        return Loader.isModLoaded(BOGO_SORT);
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
