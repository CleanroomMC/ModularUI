package com.cleanroommc.modularui;

import com.cleanroommc.modularui.drawable.DrawableSerialization;
import com.cleanroommc.modularui.holoui.HoloScreenEntity;
import com.cleanroommc.modularui.holoui.ScreenEntityRender;
import com.cleanroommc.modularui.keybind.KeyBindHandler;
import com.cleanroommc.modularui.manager.GuiInfos;
import com.cleanroommc.modularui.manager.GuiManager;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.test.EventHandler;
import com.cleanroommc.modularui.test.ItemEditorGui;
import com.cleanroommc.modularui.test.TestBlock;
import com.cleanroommc.modularui.test.tutorial.TutorialBlock;
import com.cleanroommc.modularui.theme.ThemeManager;
import com.cleanroommc.modularui.theme.ThemeReloadCommand;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.Timer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModularUI.ID,
        name = ModularUI.NAME,
        version = ModularUI.VERSION,
        dependencies = "required-after:mixinbooter@[5.0,);" +
                "after:bogorter@[1.4.0,);")
public class ModularUI {

    public static final String ID = Tags.ID;
    public static final String NAME = "Modular UI";
    public static final String VERSION = Tags.VERSION;
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final String BOGO_SORT = "bogosorter";

    @Mod.Instance
    public static ModularUI INSTANCE;

    @SideOnly(Side.CLIENT)
    private static Timer timer60Fps;

    private static boolean blurLoaded = false;
    private static boolean sorterLoaded = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(ModularUI.class);
        NetworkRegistry.INSTANCE.registerGuiHandler(ID, GuiManager.INSTANCE);
        GuiInfos.init();

        blurLoaded = Loader.isModLoaded("blur");
        sorterLoaded = Loader.isModLoaded(BOGO_SORT);

        if (NetworkUtils.isDedicatedClient()) {
            preInitClient();
        }

        if (ModularUIConfig.enabledTestGuis) {
            MinecraftForge.EVENT_BUS.register(TestBlock.class);
            TestBlock.preInit();

            MinecraftForge.EVENT_BUS.register(TutorialBlock.class);
            TutorialBlock.preInit();
        }

        NetworkHandler.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (NetworkUtils.isDedicatedClient()) {
            postInitClient();
        }
    }

    @SideOnly(Side.CLIENT)
    private void preInitClient() {
        timer60Fps = new Timer(60f);
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
        MinecraftForge.EVENT_BUS.register(KeyBindHandler.class);

        if (ModularUIConfig.enabledTestGuis) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
        }

        DrawableSerialization.init();
        RenderingRegistry.registerEntityRenderingHandler(HoloScreenEntity.class, ScreenEntityRender::new);

        // enable stencil buffer
        if (!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
            Minecraft.getMinecraft().getFramebuffer().enableStencil();
        }
    }

    @SideOnly(Side.CLIENT)
    private void postInitClient() {
        ClientCommandHandler.instance.registerCommand(new ThemeReloadCommand());
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ThemeManager());
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new ItemEditorGui.Command());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<EntityEntry> event) {
        IForgeRegistry<EntityEntry> registry = event.getRegistry();
        registry.register(EntityEntryBuilder.create()
                .id("modular_screen", 0)
                .name("ModularScreen")
                .entity(HoloScreenEntity.class)
                .factory(HoloScreenEntity::new)
                .build());
    }

    @SubscribeEvent
    public static void onCloseContainer(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof ModularContainer) {
            GuiSyncManager syncManager = ((ModularContainer) event.getContainer()).getSyncManager();
            if (syncManager != null) {
                syncManager.onOpen();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static Minecraft getMC() {
        return Minecraft.getMinecraft();
    }

    @SideOnly(Side.CLIENT)
    public static Timer getTimer60Fps() {
        return timer60Fps;
    }

    public static boolean isBlurLoaded() {
        return blurLoaded;
    }

    public static boolean isSortModLoaded() {
        return sorterLoaded;
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(ModularUI.ID)) {
            ConfigManager.sync(ModularUI.ID, Config.Type.INSTANCE);
        }
    }
}
