package com.cleanroommc.modularui;

import com.cleanroommc.modularui.factory.*;
import com.cleanroommc.modularui.holoui.HoloScreenEntity;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.test.ItemEditorGui;
import com.cleanroommc.modularui.test.TestBlock;

import com.cleanroommc.modularui.value.sync.ModularSyncManager;

import net.minecraft.util.Timer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

public class CommonProxy {

    void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(CommonProxy.class);
        MinecraftForge.EVENT_BUS.register(GuiManager.class);

        if (ModularUIConfig.enableTestGuis) {
            MinecraftForge.EVENT_BUS.register(TestBlock.class);
            TestBlock.preInit();
        }

        NetworkHandler.init();

        GuiFactories.init();
    }

    void postInit(FMLPostInitializationEvent event) {
    }

    void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new ItemEditorGui.Command());
    }

    @SideOnly(Side.CLIENT)
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

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.player.openContainer instanceof ModularContainer container) {
            container.onTick();
        }
    }
}
