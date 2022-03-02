package com.cleanroommc.modularui.common.internal;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ISyncedWidget;
import com.cleanroommc.modularui.api.IWidgetParent;
import com.cleanroommc.modularui.api.IWindowCreator;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.common.internal.network.CWidgetUpdate;
import com.cleanroommc.modularui.common.internal.network.SWidgetUpdate;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import com.cleanroommc.modularui.common.internal.wrapper.ModularUIContainer;
import com.cleanroommc.modularui.common.widget.Widget;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class ModularUIContext {

    public static final Minecraft MC = Minecraft.getMinecraft();

    private final Stack<ModularWindow> windows = new Stack<>();
    private ModularWindow mainWindow;
    @SideOnly(Side.CLIENT)
    private ModularGui screen;
    private ModularUIContainer container;
    private final EntityPlayer player;

    @SideOnly(Side.CLIENT)
    private Size screenSize = new Size(MC.displayWidth, MC.displayHeight);

    public ModularUIContext(UIBuildContext context) {
        this.player = context.player;
    }

    public boolean isClient() {
        return FMLCommonHandler.instance().getSide() == Side.CLIENT && player instanceof EntityPlayerSP;
    }

    public void initialize(ModularUIContainer container, ModularWindow mainWindow) {
        this.container = container;
        this.mainWindow = mainWindow;
        pushWindow(mainWindow);
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

    public void openWindow(IWindowCreator windowCreator) {
        pushWindow(windowCreator.create(player));
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

    @Nullable
    public Widget getTopWidgetAt(Pos2d pos) {
        return getTopWidgetAt(pos, getCurrentWindow().getChildren());
    }

    private Widget getTopWidgetAt(Pos2d pos, List<Widget> widgets) {
        Widget widgetUnderMouse = null;
        for (Widget widget : widgets) {
            if (Widget.isUnderMouse(pos, widget.getAbsolutePos(), widget.getSize())) {
                if (widget instanceof IWidgetParent) {
                    Widget childUnderMouse = getTopWidgetAt(pos, ((IWidgetParent) widget).getChildren());
                    return childUnderMouse == null ? widget : childUnderMouse;
                }
                widgetUnderMouse = widget;
            }
        }
        return widgetUnderMouse;
    }

    @SideOnly(Side.CLIENT)
    public Size getScaledScreenSize() {
        return screenSize;
    }

    public void readClientPacket(PacketBuffer buf, int widgetId) {
        ISyncedWidget syncedWidget = mainWindow.getSyncedWidget(widgetId);
        syncedWidget.readClientData(buf.readVarInt(), buf);
    }

    @SideOnly(Side.CLIENT)
    public void readServerPacket(PacketBuffer buf, int widgetId) {
        ISyncedWidget syncedWidget = mainWindow.getSyncedWidget(widgetId);
        syncedWidget.readServerData(buf.readVarInt(), buf);
    }

    @SideOnly(Side.CLIENT)
    public void sendClientPacket(int discriminator, ISyncedWidget syncedWidget, ModularWindow window, Consumer<PacketBuffer> bufferConsumer) {
        if (window != mainWindow) {
            ModularUI.LOGGER.error("Tried syncing from non main window");
            return;
        }
        int syncId = window.getSyncedWidgetId(syncedWidget);
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeVarInt(discriminator);
        bufferConsumer.accept(buffer);
        CWidgetUpdate packet = new CWidgetUpdate(buffer, syncId);
        Minecraft.getMinecraft().player.connection.sendPacket(packet);
    }

    public void sendServerPacket(int discriminator, ISyncedWidget syncedWidget, ModularWindow window, Consumer<PacketBuffer> bufferConsumer) {
        if (player instanceof EntityPlayerMP) {
            int syncId = window.getSyncedWidgetId(syncedWidget);
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeVarInt(discriminator);
            bufferConsumer.accept(buffer);
            SWidgetUpdate packet = new SWidgetUpdate(buffer, syncId);
            ((EntityPlayerMP) player).connection.sendPacket(packet);
        }
    }
}
