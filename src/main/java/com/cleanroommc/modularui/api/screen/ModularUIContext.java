package com.cleanroommc.modularui.api.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.ISyncedWidget;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.internal.network.CWidgetUpdate;
import com.cleanroommc.modularui.common.internal.network.NetworkHandler;
import com.cleanroommc.modularui.common.internal.network.NetworkUtils;
import com.cleanroommc.modularui.common.internal.network.SWidgetUpdate;
import com.cleanroommc.modularui.common.internal.wrapper.BaseSlot;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import com.cleanroommc.modularui.common.internal.wrapper.ModularUIContainer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class ModularUIContext {

    private final ImmutableMap<Integer, IWindowCreator> syncedWindowsCreators;
    private final Deque<ModularWindow> windows = new LinkedList<>();
    private final BiMap<Integer, ModularWindow> syncedWindows = HashBiMap.create(4);
    private final Map<Integer, Pos2d> lastWindowPos = new HashMap<>();
    private ModularWindow mainWindow;
    @SideOnly(Side.CLIENT)
    private ModularGui screen;
    private final EntityPlayer player;
    private final Cursor cursor;
    private final List<Widget> jeiExclusionZone = new ArrayList<>();
    private final List<Integer> queuedOpenWindow = new ArrayList<>();
    public final boolean clientOnly;
    private boolean isClosing = false;
    private final List<Runnable> closeListeners;

    private Size screenSize = NetworkUtils.isDedicatedClient() ? new Size(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight) : Size.ZERO;

    private ModularUIContainer container;

    public ModularUIContext(UIBuildContext context) {
        this(context, false);
    }

    public ModularUIContext(UIBuildContext context, boolean clientOnly) {
        this.player = context.player;
        if (!isClient() && clientOnly) {
            throw new IllegalArgumentException("Client only ModularUI can not be opened on server!");
        }
        this.clientOnly = clientOnly;
        this.syncedWindowsCreators = context.syncedWindows.build();
        this.cursor = new Cursor(this);
        this.closeListeners = context.closeListeners;
    }

    public boolean isClient() {
        return NetworkUtils.isClient(this.player);
    }

    public void initialize(ModularUIContainer container, ModularWindow mainWindow) {
        this.container = container;
        this.mainWindow = mainWindow;
        pushWindow(mainWindow);
        this.syncedWindows.put(0, mainWindow);
        mainWindow.draggable = false;
        if (isClient()) {
            // if on client, notify the server that the client initialized, to allow syncing to client
            mainWindow.initialized = true;
            sendClientPacket(DataCodes.SYNC_INIT, null, mainWindow, NetworkUtils.EMPTY_PACKET);
        }
    }

    @SideOnly(Side.CLIENT)
    public void initializeClient(ModularGui screen) {
        this.screen = screen;
    }

    @SideOnly(Side.CLIENT)
    public void buildWindowOnStart() {
        for (ModularWindow window : windows) {
            window.rebuild();
        }
    }

    @SideOnly(Side.CLIENT)
    public void resize(Size scaledSize) {
        this.screenSize = scaledSize;
        for (ModularWindow window : this.windows) {
            window.onResize(scaledSize);
            if (window == this.mainWindow) {
                getScreen().setMainWindowArea(window.getPos(), window.getSize());
            }
        }
    }

    public void onClientTick() {
        if (!queuedOpenWindow.isEmpty()) {
            queuedOpenWindow.removeIf(windowId -> {
                ModularWindow oldWindow = syncedWindows.get(windowId);
                if (oldWindow != null && oldWindow.isClosing()) return false;
                ModularWindow newWindow = openWindow(syncedWindowsCreators.get(windowId));
                syncedWindows.put(windowId, newWindow);
                newWindow.onResize(screenSize);
                newWindow.rebuild();
                newWindow.onOpen();
                newWindow.initialized = true;
                sendClientPacket(DataCodes.INIT_WINDOW, null, newWindow, NetworkUtils.EMPTY_PACKET);
                return true;
            });
        }
    }

    public boolean isWindowOpen(int id) {
        return this.syncedWindows.containsKey(id);
    }

    public void openSyncedWindow(int id) {
        if (isClient()) {
            ModularUI.LOGGER.error("Synced windows must be opened on server!");
            return;
        }
        if (isWindowOpen(id)) {
            return;
        }
        if (syncedWindowsCreators.containsKey(id)) {
            sendServerPacket(DataCodes.OPEN_WINDOW, null, mainWindow, buf -> buf.writeVarInt(id));
            ModularWindow window = openWindow(syncedWindowsCreators.get(id));
            syncedWindows.put(id, window);
        } else {
            ModularUI.LOGGER.error("Could not find window with id {}", id);
        }
    }

    public ModularWindow openWindow(IWindowCreator windowCreator) {
        ModularWindow window = windowCreator.create(player);
        pushWindow(window);
        return window;
    }

    public void closeWindow(int id) {
        if (syncedWindows.containsKey(id)) {
            closeWindow(syncedWindows.get(id));
        } else {
            ModularUI.LOGGER.error("Could not close window with id {}", id);
        }
    }

    public void closeWindow(ModularWindow window) {
        if (window == null) {
            return;
        }
        if (windows.removeLastOccurrence(window)) {
            window.destroyWindow();
        }
        if (isClient()) {
            if (!hasWindows() || window == mainWindow) {
                close();
            }
        } else {
            sendServerPacket(DataCodes.CLOSE_WINDOW, null, window, NetworkUtils.EMPTY_PACKET);
        }
        if (syncedWindows.containsValue(window)) {
            syncedWindows.inverse().remove(window);
        }
    }

    private void pushWindow(ModularWindow window) {
        if (windows.offerLast(window)) {
            window.initialize(this);
        } else {
            ModularUI.LOGGER.error("Failed opening window");
        }
    }

    public ModularWindow getCurrentWindow() {
        return windows.isEmpty() ? mainWindow : windows.peekLast();
    }

    public ModularWindow getMainWindow() {
        return mainWindow;
    }

    public void tryClose() {
        if (this.isClosing) {
            for (ModularWindow window : getOpenWindows()) {
                closeWindow(window);
            }
            return;
        }
        for (ModularWindow window : getOpenWindows()) {
            if (window.tryClose() && window == mainWindow) {
                this.isClosing = true;
            }
        }
    }

    public void close() {
        player.closeScreen();
    }

    public void closeAllButMain() {
        for (ModularWindow window : getOpenWindows()) {
            if (window != mainWindow) {
                window.tryClose();
            }
        }
    }

    public void storeWindowPos(ModularWindow window, Pos2d pos) {
        if (syncedWindows.containsValue(window)) {
            this.lastWindowPos.put(this.syncedWindows.inverse().get(window), pos);
        }
    }

    public boolean tryApplyStoredPos(ModularWindow window) {
        if (window == this.mainWindow || !this.syncedWindows.containsValue(window)) return false;
        int id = this.syncedWindows.inverse().get(window);
        if (this.lastWindowPos.containsKey(id)) {
            window.setPos(this.lastWindowPos.get(id));
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public Pos2d getMousePos() {
        return screen.getMousePos();
    }

    public boolean hasWindows() {
        return !windows.isEmpty();
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public ModularUIContainer getContainer() {
        return container;
    }

    public ModularGui getScreen() {
        return screen;
    }

    public boolean isClientOnly() {
        return clientOnly;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public Iterable<ModularWindow> getOpenWindows() {
        return windows::descendingIterator;
    }

    public Iterable<ModularWindow> getOpenWindowsReversed() {
        return windows;
    }

    @SideOnly(Side.CLIENT)
    public Size getScaledScreenSize() {
        return screenSize;
    }

    public void registerExclusionZone(Widget widget) {
        this.jeiExclusionZone.add(widget);
    }

    public List<Rectangle> getJeiExclusionZones() {
        List<Rectangle> zones = new ArrayList<>();
        for (ModularWindow window : getOpenWindows()) {
            if (window.isEnabled()) {
                zones.add(window.getRectangle());
            }
        }
        for (Widget widget : jeiExclusionZone) {
            zones.add(widget.getRectangle());
        }
        Rectangle draggableArea = cursor.getDraggableArea();
        if (draggableArea != null) {
            zones.add(draggableArea);
        }
        return zones;
    }

    public List<Runnable> getCloseListeners() {
        return closeListeners;
    }

    public void addCloseListener(Runnable runnable) {
        this.closeListeners.add(runnable);
    }

    public void syncSlotContent(BaseSlot slot) {
        if (slot != getContainer().inventorySlots.get(slot.slotNumber)) {
            throw new IllegalStateException("Slot does not have the same index in the container!");
        }
        getContainer().sendSlotChange(slot.getStack(), slot.slotNumber);
    }

    public void syncHeldItem() {
        getContainer().sendHeldItemUpdate();
    }

    public void readClientPacket(PacketBuffer buf, int widgetId) throws IOException {
        int id = buf.readVarInt();
        ModularWindow window = syncedWindows.get(buf.readVarInt());
        if (widgetId == DataCodes.INTERNAL_SYNC) {
            if (id == DataCodes.SYNC_INIT) {
                mainWindow.initialized = true;
                this.mainWindow.clientOnly = false;
            } else if (id == DataCodes.INIT_WINDOW) {
                window.initialized = true;
            } else if (id == DataCodes.CLOSE_WINDOW) {
                if (windows.removeLastOccurrence(window)) {
                    window.destroyWindow();
                }
                syncedWindows.inverse().remove(window);
            }
        } else if (window != null) {
            ISyncedWidget syncedWidget = window.getSyncedWidget(widgetId);
            syncedWidget.readOnServer(id, buf);
        }
    }

    @SideOnly(Side.CLIENT)
    public void readServerPacket(PacketBuffer buf, int widgetId) throws IOException {
        int id = buf.readVarInt();
        ModularWindow window = syncedWindows.get(buf.readVarInt());
        if (widgetId == DataCodes.INTERNAL_SYNC) {
            if (id == DataCodes.SYNC_CURSOR_STACK) {
                player.inventory.setItemStack(buf.readItemStack());
            } else if (id == DataCodes.OPEN_WINDOW) {
                queuedOpenWindow.add(buf.readVarInt());
            } else if (id == DataCodes.CLOSE_WINDOW) {
                window.tryClose();
            }
        } else if (window != null) {
            ISyncedWidget syncedWidget = window.getSyncedWidget(widgetId);
            syncedWidget.readOnClient(id, buf);
        }
    }

    @SideOnly(Side.CLIENT)
    public void sendClientPacket(int discriminator, ISyncedWidget syncedWidget, ModularWindow window, Consumer<PacketBuffer> bufferConsumer) {
        if (isClient() && !isClientOnly()) {
            if (!syncedWindows.containsValue(window)) {
                ModularUI.LOGGER.throwing(new IllegalStateException("Window is not synced!"));
                return;
            }
            int syncId = syncedWidget == null ? DataCodes.INTERNAL_SYNC : window.getSyncedWidgetId(syncedWidget);
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeVarInt(discriminator);
            buffer.writeVarInt(syncedWindows.inverse().get(window));
            bufferConsumer.accept(buffer);
            CWidgetUpdate packet = new CWidgetUpdate(buffer, syncId);
            NetworkHandler.sendToServer(packet);
        }
    }

    public void sendServerPacket(int discriminator, ISyncedWidget syncedWidget, ModularWindow window, Consumer<PacketBuffer> bufferConsumer) {
        if (!isClient()) {
            if (!syncedWindows.containsValue(window)) {
                ModularUI.LOGGER.throwing(new IllegalStateException("Window is not synced!"));
                return;
            }
            int syncId = syncedWidget == null ? DataCodes.INTERNAL_SYNC : window.getSyncedWidgetId(syncedWidget);
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeVarInt(discriminator);
            buffer.writeVarInt(syncedWindows.inverse().get(window));
            bufferConsumer.accept(buffer);
            SWidgetUpdate packet = new SWidgetUpdate(buffer, syncId);
            NetworkHandler.sendToPlayer(packet, (EntityPlayerMP) player);
        }
    }

    protected static class DataCodes {
        static final int INTERNAL_SYNC = -1;
        static final int SYNC_CURSOR_STACK = 1;
        static final int SYNC_INIT = 2;
        static final int OPEN_WINDOW = 3;
        static final int INIT_WINDOW = 4;
        static final int CLOSE_WINDOW = 5;
    }
}
