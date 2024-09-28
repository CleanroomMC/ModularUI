package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.JeiSettings;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.packets.OpenGuiPacket;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

    private static GuiScreenWrapper lastMui;
    private static final List<Player> openedContainers = new ArrayList<>(4);

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

    public static <T extends GuiData> void open(@NotNull UIFactory<T> factory, @NotNull T guiData, ServerPlayer player) {
        if (player instanceof FakePlayer || openedContainers.contains(player)) return;
        openedContainers.add(player);
        // create panel, collect sync handlers and create container
        guiData.setJeiSettings(JeiSettings.DUMMY);
        PanelSyncManager syncManager = new PanelSyncManager();
        ModularPanel panel = factory.createPanel(guiData, syncManager);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularContainer container = new ModularContainer(player, syncManager, panel.getName());
        // sync to client
        player.nextContainerCounter();
        player.closeContainer();
        int windowId = player.containerCounter;
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        factory.writeGuiData(guiData, buffer);
        NetworkHandler.sendToPlayer(new OpenGuiPacket<>(windowId, factory, buffer), player);
        // open container // this mimics forge behaviour
        player.containerMenu = container;
        player.containerMenu.containerId = windowId;
        player.containerMenu.addSlotListener(player.containerListener);
        // finally invoke event
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends GuiData> void open(int windowId, @NotNull UIFactory<T> factory, @NotNull FriendlyByteBuf data, @NotNull Player player) {
        T guiData = factory.readGuiData(player, data);
        JeiSettingsImpl jeiSettings = new JeiSettingsImpl();
        guiData.setJeiSettings(jeiSettings);
        PanelSyncManager syncManager = new PanelSyncManager();
        ModularPanel panel = factory.createPanel(guiData, syncManager);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularScreen screen = factory.createScreen(guiData, panel);
        screen.getContext().setJeiSettings(jeiSettings);
        GuiScreenWrapper guiScreenWrapper = new GuiScreenWrapper(new ModularContainer(player, syncManager, panel.getName()), screen, player.getInventory(), );
        guiScreenWrapper.getMenu().containerId = windowId;
        Minecraft.getInstance().setScreen(guiScreenWrapper);
        player.containerMenu = guiScreenWrapper.getMenu();
    }

    @OnlyIn(Dist.CLIENT)
    static void openScreen(ModularScreen screen, JeiSettingsImpl jeiSettings, ContainerCustomizer containerCustomizer) {
        screen.getContext().setJeiSettings(jeiSettings);
        GuiScreenWrapper screenWrapper = new GuiScreenWrapper(new ModularContainer(containerCustomizer), screen, null);
        Minecraft.getInstance().setScreen(screenWrapper);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            openedContainers.clear();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onGuiOpen(ScreenEvent event) {
        if (lastMui != null && event.getScreen() == null) {
            if (lastMui.getScreen().getPanelManager().isOpen()) {
                lastMui.getScreen().getPanelManager().closeAll();
            }
            lastMui.getScreen().getPanelManager().dispose();
            lastMui = null;
        } else if (event.getScreen() instanceof GuiScreenWrapper screenWrapper) {
            if (lastMui == null) {
                lastMui = screenWrapper;
            } else if (lastMui == event.getScreen()) {
                lastMui.getScreen().getPanelManager().reopen();
            } else {
                if (lastMui.getScreen().getPanelManager().isOpen()) {
                    lastMui.getScreen().getPanelManager().closeAll();
                }
                lastMui.getScreen().getPanelManager().dispose();
                lastMui = screenWrapper;
            }
        }
    }
}
