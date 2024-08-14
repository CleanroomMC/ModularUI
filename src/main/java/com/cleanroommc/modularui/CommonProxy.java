package com.cleanroommc.modularui;

import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.ItemGuiFactory;
import com.cleanroommc.modularui.factory.SidedTileEntityGuiFactory;
import com.cleanroommc.modularui.factory.TileEntityGuiFactory;
import com.cleanroommc.modularui.holoui.HoloScreenEntity;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.test.ItemEditorGui;
import com.cleanroommc.modularui.test.TestBlock;

import com.cleanroommc.modularui.value.sync.ModularSyncManager;

import net.minecraft.client.Timer;
import net.minecraft.util.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

public class CommonProxy {

    public CommonProxy() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);

        eventBus.register(CommonProxy.class);
        eventBus.register(GuiManager.class);

        if (ModularUIConfig.enabledTestGuis) {
            eventBus.register(TestBlock.class);
            TestBlock.preInit();
        }

        NetworkHandler.init();

        GuiManager.registerFactory(TileEntityGuiFactory.INSTANCE);
        GuiManager.registerFactory(SidedTileEntityGuiFactory.INSTANCE);
        GuiManager.registerFactory(ItemGuiFactory.INSTANCE);
    }

    @SubscribeEvent
    void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new ItemEditorGui.Command());
    }

    @OnlyIn(Dist.CLIENT)
    public Timer getTimer60Fps() {
        throw new UnsupportedOperationException();
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
        if (event.getContainer() instanceof ModularContainer container) {
            ModularSyncManager syncManager = container.getSyncManager();
            if (syncManager != null) {
                syncManager.onOpen();
            }
        }
    }

    @SubscribeEvent
    public static void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(ModularUI.ID)) {
            ConfigManager.sync(ModularUI.ID, Config.Type.INSTANCE);
        }
    }
}
