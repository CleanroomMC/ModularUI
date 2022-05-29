package com.cleanroommc.modularui;

import com.cleanroommc.modularui.api.UIInfos;
import com.cleanroommc.modularui.api.screen.ModularUIContext;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.internal.JsonLoader;
import com.cleanroommc.modularui.common.internal.network.NetworkHandler;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import com.cleanroommc.modularui.common.internal.wrapper.ModularUIContainer;
import com.cleanroommc.modularui.common.widget.WidgetJsonRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

@Mod(modid = ModularUI.ID, name = ModularUI.NAME, version = ModularUI.VERSION)
public class ModularUI {

    public static final String ID = "modularui";
    public static final String NAME = "Modular UI";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final String INV_TWEAKS = "inventorytweaks";

    @Mod.Instance
    public static ModularUI INSTANCE;

    @SidedProxy(modId = ID, clientSide = "com.cleanroommc.modularui.ClientProxy", serverSide = "com.cleanroommc.modularui.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();
        NetworkHandler.init();
        UIInfos.init();
        WidgetJsonRegistry.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
            JsonLoader.loadJson();
        }
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    public static boolean isInvTweaksLoaded() {
        return Loader.isModLoaded(INV_TWEAKS);
    }

    public static ModularUIContainer createContainer(EntityPlayer player, Function<UIBuildContext, ModularWindow> windowCreator) {
        UIBuildContext buildContext = new UIBuildContext(player);
        ModularWindow window = windowCreator.apply(buildContext);
        return new ModularUIContainer(new ModularUIContext(buildContext), window);
    }

    @SideOnly(Side.CLIENT)
    public static ModularGui createGuiScreen(EntityPlayer player, Function<UIBuildContext, ModularWindow> windowCreator) {
        return new ModularGui(createContainer(player, windowCreator));
    }
}
