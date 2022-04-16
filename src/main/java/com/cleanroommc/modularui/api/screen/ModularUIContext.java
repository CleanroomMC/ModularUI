package com.cleanroommc.modularui.api.screen;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.ISyncedWidget;
import com.cleanroommc.modularui.api.widget.IWidgetParent;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.internal.network.CWidgetUpdate;
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
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ModularUIContext {

    public static final Minecraft MC = Minecraft.getMinecraft();

    private final ImmutableMap<Integer, IWindowCreator> syncedWindowsCreators;
    private final Deque<ModularWindow> windows = new LinkedList<>();
    private final BiMap<Integer, ModularWindow> syncedWindows = HashBiMap.create(4);
    private ModularWindow mainWindow;
    @SideOnly(Side.CLIENT)
    private ModularGui screen;
    private ModularUIContainer container;
    private final EntityPlayer player;

    private boolean oneSided = true;

    @SideOnly(Side.CLIENT)
    private Size screenSize = new Size(MC.displayWidth, MC.displayHeight);

    public ModularUIContext(UIBuildContext context) {
        this.player = context.player;
        this.syncedWindowsCreators = context.syncedWindows.build();
    }

    public boolean isClient() {
        return player.world != null ? player.world.isRemote : player instanceof EntityPlayerSP;
    }

    public void initialize(ModularUIContainer container, ModularWindow mainWindow) {
        this.container = container;
        this.mainWindow = mainWindow;
        pushWindow(mainWindow);
        this.syncedWindows.put(0, mainWindow);
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
        getCurrentWindow().rebuild();
    }

    @SideOnly(Side.CLIENT)
    public void resize(Size scaledSize) {
        this.screenSize = scaledSize;
        for (ModularWindow window : windows) {
            window.onResize(scaledSize);
        }
    }

    public void openSyncedWindow(int id) {
        if (syncedWindowsCreators.containsKey(id)) {
            if (isClient()) {
                sendClientPacket(DataCodes.OPEN_WINDOW, null, mainWindow, buf -> buf.writeVarInt(id));
            } else {
                sendServerPacket(DataCodes.OPEN_WINDOW, null, mainWindow, buf -> buf.writeVarInt(id));
            }
            ModularWindow window = openWindow(syncedWindowsCreators.get(id));
            syncedWindows.put(id, window);
            if (isClient()) {
                window.initialized = true;
            }
        } else {
            ModularUI.LOGGER.error("Could not find window with id {}", id);
        }
    }

    public ModularWindow openWindow(IWindowCreator windowCreator) {
        ModularWindow window = windowCreator.create(player);
        pushWindow(window);
        if (isClient()) {
            window.onResize(screenSize);
            window.onOpen();
        }
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
            window.closeWindow();
        }
        if (syncedWindows.containsValue(window)) {
            syncedWindows.inverse().remove(window);
        }
        if (isClient()) {
            sendClientPacket(DataCodes.CLOSE_WINDOW, null, window, NetworkUtils.EMPTY_PACKET);
        } else {
            sendServerPacket(DataCodes.CLOSE_WINDOW, null, window, NetworkUtils.EMPTY_PACKET);
        }
    }

    private void pushWindow(ModularWindow window) {
        window.initialize(this);
        window.setActive(true);
        if (hasWindows()) {
            getCurrentWindow().pauseWindow();
        }
        windows.push(window);
    }

    public void popWindow() {
        getCurrentWindow().closeWindow();
        windows.pop();
        if (hasWindows()) {
            getCurrentWindow().resumeWindow();
        }
    }

    public ModularWindow getCurrentWindow() {
        return windows.peek();
    }

    public ModularWindow getMainWindow() {
        return mainWindow;
    }

    public void tryClose() {
        if (mainWindow.onTryClose()) {
            close();
        }
    }

    public void close() {
        player.closeScreen();
    }

    public void popAllButLast() {
        while (windows.size() >= 2) {
            popWindow();
        }
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

    public ItemStack getCursorStack() {
        return player.inventory.getItemStack();
    }

    public void setCursorStack(ItemStack stack, boolean sync) {
        if (stack != null) {
            player.inventory.setItemStack(stack);
            if (sync && !isClient()) {
                sendServerPacket(DataCodes.SYNC_CURSOR_STACK, null, mainWindow, buffer -> buffer.writeItemStack(stack));
            }
        }
    }

    public ModularUIContainer getContainer() {
        return container;
    }

    public ModularGui getScreen() {
        return screen;
    }

    public boolean isOneSided() {
        return oneSided;
    }

    @Nullable
    public Widget getTopWidgetAt(Pos2d pos) {
        return getTopWidgetAt(pos, getCurrentWindow().getChildren());
    }

    private Widget getTopWidgetAt(Pos2d pos, List<Widget> widgets) {
        Widget widgetUnderMouse = null;
        for (Widget widget : widgets) {
            if (!widget.isEnabled()) {
                continue;
            }
            if ((widgetUnderMouse == null || widgetUnderMouse.getLayer() <= widget.getLayer()) && Widget.isUnderMouse(pos, widget.getAbsolutePos(), widget.getSize())) {
                widgetUnderMouse = widget;
            }
            if (widget instanceof IWidgetParent) {
                Widget childUnderMouse = getTopWidgetAt(pos, ((IWidgetParent) widget).getChildren());
                if (childUnderMouse != null && (widgetUnderMouse == null || widgetUnderMouse.getLayer() <= childUnderMouse.getLayer())) {
                    widgetUnderMouse = childUnderMouse;
                }
            }
        }
        return widgetUnderMouse;
    }

    @SideOnly(Side.CLIENT)
    public Size getScaledScreenSize() {
        return screenSize;
    }

    public void syncSlotContent(BaseSlot slot) {
        if (slot != getContainer().inventorySlots.get(slot.slotNumber)) {
            throw new IllegalStateException("Slot does not have the same index in the container!");
        }
        getContainer().sendSlotChange(slot.getStack(), slot.slotNumber);
    }

    public void readClientPacket(PacketBuffer buf, int widgetId) throws IOException {
        this.oneSided = false;
        int id = buf.readVarInt();
        ModularWindow window = syncedWindows.get(buf.readVarInt());
        if (widgetId == DataCodes.INTERNAL_SYNC) {
            if (id == DataCodes.SYNC_INIT) {
                mainWindow.initialized = true;
                this.mainWindow.clientOnly = false;
            } else if (id == DataCodes.OPEN_WINDOW) {
                pushWindow(syncedWindows.get(buf.readVarInt()));
                ModularWindow newWindow = openWindow(syncedWindowsCreators.get(id));
                syncedWindows.put(id, newWindow);
            } else if (id == DataCodes.INIT_WINDOW) {
                window.initialized = true;
            } else if (id == DataCodes.CLOSE_WINDOW) {
                if (windows.removeLastOccurrence(window)) {
                    window.closeWindow();
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
        this.oneSided = false;
        int id = buf.readVarInt();
        ModularWindow window = syncedWindows.get(buf.readVarInt());
        if (widgetId == DataCodes.INTERNAL_SYNC) {
            if (id == DataCodes.SYNC_CURSOR_STACK) {
                player.inventory.setItemStack(buf.readItemStack());
            } else if (id == DataCodes.OPEN_WINDOW) {
                int windowId = buf.readVarInt();
                ModularWindow newWindow = openWindow(syncedWindowsCreators.get(windowId));
                syncedWindows.put(windowId, newWindow);
                newWindow.initialized = true;
                sendClientPacket(DataCodes.INIT_WINDOW, null, window, NetworkUtils.EMPTY_PACKET);
            } else if (id == DataCodes.CLOSE_WINDOW) {
                if (windows.removeLastOccurrence(window)) {
                    window.closeWindow();
                }
                syncedWindows.inverse().remove(window);
            }
        } else if (window != null) {
            ISyncedWidget syncedWidget = window.getSyncedWidget(widgetId);
            syncedWidget.readOnClient(id, buf);
        }
    }

    @SideOnly(Side.CLIENT)
    public void sendClientPacket(int discriminator, ISyncedWidget syncedWidget, ModularWindow window, Consumer<PacketBuffer> bufferConsumer) {
        if (isClient()) {
            if (!syncedWindows.containsValue(window)) {
                ModularUI.LOGGER.error("Window is not synced!");
                return;
            }
            int syncId = syncedWidget == null ? DataCodes.INTERNAL_SYNC : window.getSyncedWidgetId(syncedWidget);
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeVarInt(discriminator);
            buffer.writeVarInt(syncedWindows.inverse().get(window));
            bufferConsumer.accept(buffer);
            CWidgetUpdate packet = new CWidgetUpdate(buffer, syncId);
            Minecraft.getMinecraft().player.connection.sendPacket(packet);
        }
    }

    public void sendServerPacket(int discriminator, ISyncedWidget syncedWidget, ModularWindow window, Consumer<PacketBuffer> bufferConsumer) {
        if (!isClient()) {
            if (!syncedWindows.containsValue(window)) {
                ModularUI.LOGGER.error("Window is not synced!");
                return;
            }
            int syncId = syncedWidget == null ? DataCodes.INTERNAL_SYNC : window.getSyncedWidgetId(syncedWidget);
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeVarInt(discriminator);
            buffer.writeVarInt(syncedWindows.inverse().get(window));
            bufferConsumer.accept(buffer);
            SWidgetUpdate packet = new SWidgetUpdate(buffer, syncId);
            ((EntityPlayerMP) player).connection.sendPacket(packet);
        }
    }

    private static class DataCodes {
        static final int INTERNAL_SYNC = -1;
        static final int SYNC_CURSOR_STACK = 1;
        static final int SYNC_INIT = 2;
        static final int OPEN_WINDOW = 3;
        static final int INIT_WINDOW = 4;
        static final int CLOSE_WINDOW = 5;
    }
}
