package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.JeiSettings;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.holoui.HoloUI;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.packets.OpenGuiPacket;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HoloGuiManager extends GuiManager {


    private static GuiScreenWrapper lastMui;
    private static final List<EntityPlayer> openedContainers = new ArrayList<>(4);
    private static final Map<UUID, List<Data>> map = new Object2ObjectOpenHashMap<>();

    public static <T extends GuiData> void open(@NotNull UIFactory<T> factory, @NotNull T guiData, EntityPlayerMP player) {
        if (player instanceof FakePlayer || openedContainers.contains(player)) return;
        openedContainers.add(player);
        // create panel, collect sync handlers and create container
        guiData.setJeiSettings(JeiSettings.DUMMY);
        GuiSyncManager syncManager = new GuiSyncManager(player);
        ModularPanel panel = factory.createPanel(guiData, syncManager);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularContainer container = new ModularContainer(syncManager);
        map.computeIfAbsent(player.getUniqueID(), uuid -> new ArrayList<>())
                .add(new Data(container, panel, null));
        // sync to client
//        player.getNextWindowId();
//        player.closeContainer();
//        int windowId = player.currentWindowId;
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        factory.writeGuiData(guiData, buffer);
        NetworkHandler.sendToPlayer(new OpenGuiPacket<>(0, factory, buffer), player);
        // open container // this mimics forge behaviour
//        player.openContainer = container;
//        player.openContainer.windowId = windowId;
//        player.openContainer.addListener(player);
        // finally invoke event
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
    }

    @SideOnly(Side.CLIENT)
    public static <T extends GuiData> void open(int windowId, @NotNull UIFactory<T> factory, @NotNull PacketBuffer data, @NotNull EntityPlayerSP player) {
        T guiData = factory.readGuiData(player, data);
        JeiSettingsImpl jeiSettings = new JeiSettingsImpl();
        guiData.setJeiSettings(jeiSettings);
        GuiSyncManager syncManager = new GuiSyncManager(player);
        ModularPanel panel = factory.createPanel(guiData, syncManager);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularScreen screen = factory.createScreen(guiData, panel);
        screen.getContext().setJeiSettings(jeiSettings);
        GuiScreenWrapper guiScreenWrapper = new GuiScreenWrapper(new ModularContainer(syncManager), screen);
        guiScreenWrapper.inventorySlots.windowId = windowId;
        map.computeIfAbsent(player.getUniqueID(), uuid -> new ArrayList<>())
                        .add(new Data(screen.getContainer(), panel, screen));
        HoloUI.builder()
                .inFrontOf(player, 5, true)
                .open(guiScreenWrapper);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onClick(InputEvent.MouseInputEvent event) {
        var mouse = MouseData.create(Mouse.getEventButton());
        boolean pressed = Mouse.getEventButtonState();
        var player = Minecraft.getMinecraft().player;
        if (player != null && mouse.mouseButton != -1 && pressed) {
            for (var data : map.get(player.getUniqueID())) {
                ModularUI.LOGGER.warn("click");
            }
        }
    }

    private static class Data {
        @Nullable
        public ModularScreen screen;
        public ModularPanel panel;
        public ModularContainer container;
        protected Data(ModularContainer container, ModularPanel panel, @Nullable ModularScreen screen) {
            this.container = container;
            this.panel = panel;
            this.screen = screen;
        }
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent event) {
        if (lastMui != null && event.getGui() == null) {
            if (lastMui.getScreen().getPanelManager().isOpen()) {
                lastMui.getScreen().getPanelManager().closeAll();
            }
            lastMui.getScreen().getPanelManager().dispose();
            lastMui = null;
        } else if (event.getGui() instanceof GuiScreenWrapper screenWrapper) {
            if (lastMui == null) {
                lastMui = screenWrapper;
            } else if (lastMui == event.getGui()) {
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
