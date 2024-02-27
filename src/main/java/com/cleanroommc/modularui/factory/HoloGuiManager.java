package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.JeiSettings;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.holoui.HoloUI;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.packets.OpenGuiPacket;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;

import io.netty.buffer.Unpooled;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HoloGuiManager extends GuiManager {


    private static GuiScreenWrapper lastMui;
    private static final List<EntityPlayer> openedContainers = new ArrayList<>(4);

    public static <T extends GuiData> void open(@NotNull UIFactory<T> factory, @NotNull T guiData, EntityPlayerMP player) {
        if (player instanceof FakePlayer || openedContainers.contains(player)) return;
        openedContainers.add(player);
        // create panel, collect sync handlers and create container
        guiData.setJeiSettings(JeiSettings.DUMMY);
        GuiSyncManager syncManager = new GuiSyncManager(player);
        ModularPanel panel = factory.createPanel(guiData, syncManager);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularContainer container = new ModularContainer(syncManager);
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
        JeiSettingsImpl jeiSettings = new JeiSettingsImpl();
        guiData.setJeiSettings(jeiSettings);
        GuiSyncManager syncManager = new GuiSyncManager(player);
        ModularPanel panel = factory.createPanel(guiData, syncManager);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularScreen screen = factory.createScreen(guiData, panel);
        screen.getContext().setJeiSettings(jeiSettings);
        GuiScreenWrapper guiScreenWrapper = new GuiScreenWrapper(new ModularContainer(syncManager), screen);
        guiScreenWrapper.inventorySlots.windowId = windowId;
//        Minecraft.getMinecraft().displayGuiScreen(guiScreenWrapper);
        HoloUI.builder()
                .inFrontOf(player, 5, true)
                .open(guiScreenWrapper);
        player.openContainer = guiScreenWrapper.inventorySlots;
    }

    @SideOnly(Side.CLIENT)
    static void openScreen(ModularScreen screen, JeiSettingsImpl jeiSettings) {
        screen.getContext().setJeiSettings(jeiSettings);
        GuiScreenWrapper screenWrapper = new GuiScreenWrapper(new ModularContainer(), screen);
        Minecraft.getMinecraft().displayGuiScreen(screenWrapper);
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
