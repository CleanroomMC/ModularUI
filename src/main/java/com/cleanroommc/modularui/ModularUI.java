package com.cleanroommc.modularui;

import com.cleanroommc.modularui.keybind.KeyBindHandler;
import com.cleanroommc.modularui.manager.GuiManager;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.test.EventHandler;
import com.cleanroommc.modularui.test.TestBlock;
import com.cleanroommc.modularui.utils.Animator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
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

    public static final Timer TIMER_60_FPS = new Timer(60f);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(ID, GuiManager.INSTANCE);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
            MinecraftForge.EVENT_BUS.register(KeyBindHandler.class);
        }

        if (FMLLaunchHandler.isDeobfuscatedEnvironment()) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
            MinecraftForge.EVENT_BUS.register(TestBlock.class);
            TestBlock.preInit();
        }

        NetworkHandler.init();
    }

    public static boolean isSortModLoaded() {
        return Loader.isModLoaded(BOGO_SORT);
    }

    public static Minecraft getMC() {
        return Minecraft.getMinecraft();
    }


}
