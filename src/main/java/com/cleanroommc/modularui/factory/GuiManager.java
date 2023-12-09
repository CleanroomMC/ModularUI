package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.JeiSettings;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.packets.OpenGuiPacket;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.NoSuchElementException;
import java.util.Objects;

public class GuiManager {

    private static final Object2ObjectMap<String, UIFactory<?>> FACTORIES = new Object2ObjectOpenHashMap<>(16);
    private static ModularScreen queuedClientScreen;
    private static JeiSettingsImpl queuedJeiSettings;
    private static GuiScreen queuedGuiScreen;
    private static boolean closeScreen;
    private static boolean openingQueue = false;

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

    public static UIFactory<?> getFactory(String name) {
        UIFactory<?> factory = FACTORIES.get(name);
        if (factory == null) throw new NoSuchElementException();
        return factory;
    }

    public static <T extends GuiData> void open(UIFactory<T> factory, T guiData, EntityPlayerMP player) {
        // create panel, collect sync handlers and create container
        guiData.setJeiSettings(JeiSettings.DUMMY);
        GuiSyncManager syncManager = new GuiSyncManager(player);
        ModularPanel panel = factory.createPanel(guiData, syncManager);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularContainer container = new ModularContainer(syncManager);
        // open container // this mimics forge behaviour
        player.getNextWindowId();
        player.closeContainer();
        int windowId = player.currentWindowId;
        player.openContainer = container;
        player.openContainer.windowId = windowId;
        player.openContainer.addListener(player);
        // sync to client
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        factory.writeGuiData(guiData, buffer);
        NetworkHandler.sendToPlayer(new OpenGuiPacket<>(windowId, factory, buffer), player);
    }

    @SideOnly(Side.CLIENT)
    public static <T extends GuiData> void open(int windowId, UIFactory<T> factory, PacketBuffer data, EntityPlayerSP player) {
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
        Minecraft.getMinecraft().displayGuiScreen(guiScreenWrapper);
    }

    @SideOnly(Side.CLIENT)
    public static void checkQueuedScreen() {
        openingQueue = true;
        if (queuedClientScreen != null) {
            queuedClientScreen.getContext().setJeiSettings(queuedJeiSettings);
            GuiScreenWrapper screenWrapper = new GuiScreenWrapper(new ModularContainer(), queuedClientScreen);
            Minecraft.getMinecraft().displayGuiScreen(screenWrapper);
        } else if (queuedGuiScreen != null) {
            Minecraft.getMinecraft().displayGuiScreen(queuedGuiScreen);
        } else if (closeScreen) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
        queuedClientScreen = null;
        queuedJeiSettings = null;
        queuedGuiScreen = null;
        closeScreen = false;
        openingQueue = false;
    }

    @SideOnly(Side.CLIENT)
    static void openScreen(ModularScreen screen, JeiSettingsImpl jeiSettings) {
        queuedClientScreen = screen;
        queuedJeiSettings = jeiSettings;
        queuedGuiScreen = null;
        closeScreen = false;
    }

    @SideOnly(Side.CLIENT)
    static void openScreen(GuiScreen screen) {
        queuedClientScreen = null;
        queuedJeiSettings = null;
        queuedGuiScreen = screen;
        closeScreen = false;
    }

    @SideOnly(Side.CLIENT)
    static void closeScreen() {
        queuedClientScreen = null;
        queuedJeiSettings = null;
        queuedGuiScreen = null;
        closeScreen = true;
    }

    public static boolean isOpeningQueue() {
        return openingQueue;
    }
}
