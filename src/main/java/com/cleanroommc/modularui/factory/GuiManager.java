package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IMuiScreen;
import com.cleanroommc.modularui.api.JeiSettings;
import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.packets.OpenGuiPacket;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class GuiManager {

    private static final Object2ObjectMap<String, UIFactory<?>> FACTORIES = new Object2ObjectOpenHashMap<>(16);

    private static final List<EntityPlayer> openedContainers = new ArrayList<>(4);

    public static void registerFactory(UIFactory<?> factory) {
        Objects.requireNonNull(factory);
        String name = Objects.requireNonNull(factory.getFactoryName());
        if (name.length() > 32) {
            throw new IllegalArgumentException("The factory name length must not exceed 32!");
        }
        if (FACTORIES.containsKey(name)) {
            throw new IllegalArgumentException("Factory with name '" + name + "' is already registered!");
        }
        FACTORIES.put(name, factory);
    }

    public static @NotNull UIFactory<?> getFactory(String name) {
        UIFactory<?> factory = FACTORIES.get(name);
        if (factory == null) throw new NoSuchElementException();
        return factory;
    }

    public static boolean hasFactory(String name) {
        return FACTORIES.containsKey(name);
    }

    public static <T extends GuiData> void open(@NotNull UIFactory<T> factory, @NotNull T guiData, EntityPlayerMP player) {
        if (player instanceof FakePlayer || openedContainers.contains(player)) return;
        openedContainers.add(player);
        // create panel, collect sync handlers and create container
        UISettings settings = new UISettings(JeiSettings.DUMMY);
        settings.defaultCanInteractWith(factory, guiData);
        PanelSyncManager syncManager = new PanelSyncManager();
        ModularPanel panel = factory.createPanel(guiData, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularContainer container = settings.hasContainer() ? settings.createContainer() : factory.createContainer();
        container.construct(player, syncManager, settings, panel.getName(), guiData);
        // sync to client
        player.getNextWindowId();
        player.closeContainer();
        int windowId = player.currentWindowId;
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        factory.writeGuiData(guiData, buffer);
        NetworkHandler.sendToPlayer(new OpenGuiPacket<>(windowId, factory, buffer), player);
        // open container // this mimics forge behaviour
        player.openContainer = container;
        player.openContainer.windowId = windowId;
        player.openContainer.addListener(player);
        // finally invoke event
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
    }

    @SideOnly(Side.CLIENT)
    public static <T extends GuiData> void open(int windowId, @NotNull UIFactory<T> factory, @NotNull PacketBuffer data, @NotNull EntityPlayerSP player) {
        T guiData = factory.readGuiData(player, data);
        UISettings settings = new UISettings();
        settings.defaultCanInteractWith(factory, guiData);
        PanelSyncManager syncManager = new PanelSyncManager();
        ModularPanel panel = factory.createPanel(guiData, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularScreen screen = factory.createScreen(guiData, panel);
        screen.getContext().setSettings(settings);
        ModularContainer container = settings.hasContainer() ? settings.createContainer() : factory.createContainer();
        container.construct(player, syncManager, settings, panel.getName(), guiData);
        IMuiScreen wrapper = factory.createScreenWrapper(container, screen);
        if (!(wrapper.getGuiScreen() instanceof GuiContainer guiContainer)) {
            throw new IllegalStateException("The wrapping screen must be a GuiContainer for synced GUIs!");
        }
        if (guiContainer.inventorySlots != container) throw new IllegalStateException("Custom Containers are not yet allowed!");
        guiContainer.inventorySlots.windowId = windowId;
        MCHelper.displayScreen(wrapper.getGuiScreen());
        player.openContainer = guiContainer.inventorySlots;
    }

    @SideOnly(Side.CLIENT)
    static void openScreen(ModularScreen screen, UISettings settings) {
        screen.getContext().setSettings(settings);
        GuiScreen guiScreen;
        if (settings.hasContainer()) {
            ModularContainer container = settings.createContainer();
            container.constructClientOnly();
            guiScreen = new GuiContainerWrapper(container, screen);
        } else {
            guiScreen = new GuiScreenWrapper(screen);
        }
        MCHelper.displayScreen(guiScreen);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            openedContainers.clear();
        }
    }
}
