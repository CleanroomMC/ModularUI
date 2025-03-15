package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.JeiSettings;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.holoui.HoloScreenEntity;
import com.cleanroommc.modularui.holoui.HoloUI;
import com.cleanroommc.modularui.holoui.ScreenEntityRender;
import com.cleanroommc.modularui.network.NetworkHandler;
import com.cleanroommc.modularui.network.packets.OpenGuiPacket;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoloGuiManager extends GuiManager {


    private static GuiScreenWrapper lastMui;

    public static <T extends GuiData> void open(@NotNull UIFactory<T> factory, @NotNull T guiData, EntityPlayerMP player) {
        if (player instanceof FakePlayer) return;
        // create panel, collect sync handlers and create container
        guiData.setJeiSettings(JeiSettings.DUMMY);
        PanelSyncManager syncManager = new PanelSyncManager();
        ModularPanel panel = factory.createPanel(guiData, syncManager);
        List<HoloScreenEntity> screens = player.world.getEntities(HoloScreenEntity.class, entity -> entity.isName(panel.getName()));
        if (!screens.isEmpty()) {
            for (HoloScreenEntity screen : screens) {
                screen.setDead();
            }
            /*HoloUI.builder()
                    .inFrontOf(player, 5, true)
                    .reposition(player, screens);
            NetworkHandler.sendToPlayer(new SyncHoloPacket(panel.getName()), player);
            ModularUI.LOGGER.warn("reposition the holo, sync to client");
            return;*/
        }
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularContainer container = new ModularContainer(player, syncManager, panel.getName());
        HoloUI.builder()
                .screenScale(0.5f)
                .inFrontOf(player, 5, true)
                .open(screen -> {
                    screen.setPanel(panel);
                    //HoloUI.registerSyncedHoloUI(panel, screen);
                }, player.getEntityWorld());
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
        PanelSyncManager syncManager = new PanelSyncManager();
        ModularPanel panel = factory.createPanel(guiData, syncManager);
        WidgetTree.collectSyncValues(syncManager, panel);
        ModularScreen screen = factory.createScreen(guiData, panel);
        screen.getContext().setJeiSettings(jeiSettings);
        GuiContainerWrapper guiScreenWrapper = new GuiContainerWrapper(new ModularContainer(player, syncManager, panel.getName()), screen);
        guiScreenWrapper.inventorySlots.windowId = windowId;
        HoloUI.builder()
//                .screenScale(0.25f)
                .inFrontOf(player, 5, true)
                .screenScale(0.5f)
                .open(screen1 -> {
                    screen1.setPanel(panel);
                    screen1.setWrapper(guiScreenWrapper);
                }, player.getEntityWorld());
    }

    public static void reposition(String panel, EntityPlayer player) {
        HoloUI.builder()
//                .screenScale(0.25f)
                .inFrontOf(player, 5, true)
                .reposition(player, player.world.getEntities(HoloScreenEntity.class, entity -> entity.isName(panel)));
    }

    //todo make this a mixin instead of using event to cancel arm animation stuff
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onClick(InputEvent.MouseInputEvent event) {
        var player = Minecraft.getMinecraft().player;
        if (player != null && player.getHeldItemMainhand().isEmpty() && player.getHeldItemOffhand().isEmpty()) {
            ScreenEntityRender.clickScreen(player);
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
