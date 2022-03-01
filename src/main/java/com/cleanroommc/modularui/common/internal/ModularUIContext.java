package com.cleanroommc.modularui.common.internal;

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
import net.minecraft.client.Minecraft;
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

    public static boolean isClient() {
        return FMLCommonHandler.instance().getSide() == Side.CLIENT;
    }

    private final Stack<ModularWindow> windows = new Stack<>();
    @SideOnly(Side.CLIENT)
    private ModularGui screen;
    private ModularUIContainer container;
    private final EntityPlayer player;

    @SideOnly(Side.CLIENT)
    private Size screenSize = new Size(MC.displayWidth, MC.displayHeight);

    public ModularUIContext(UIBuildContext context) {
        this.player = context.player;
    }

    public void initialize(ModularUIContainer container, ModularWindow mainWindow) {
        this.container = container;
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
        for (Widget widget : widgets) {
            if (Widget.isUnderMouse(pos, widget.getAbsolutePos(), widget.getSize())) {
                if (widget instanceof IWidgetParent) {
                    Widget childUnderMouse = getTopWidgetAt(pos, ((IWidgetParent) widget).getChildren());
                    return childUnderMouse == null ? widget : childUnderMouse;
                }
                return widget;
            }
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public Size getScaledScreenSize() {
        return screenSize;
    }

    public void readClientPacket(PacketBuffer buf) {

    }

    @SideOnly(Side.CLIENT)
    public void readServerPacket(PacketBuffer buf) {

    }

    @SideOnly(Side.CLIENT)
    public void sendClientPacket(int discriminator, ISyncedWidget syncedWidget, ModularWindow window, Consumer<PacketBuffer> bufferConsumer) {

        int syncId = window.getSyncedWidgetId(syncedWidget);
        Consumer<PacketBuffer> buffer = buf -> {
            buf.writeVarInt(syncId);
            buf.writeVarInt(discriminator);
            bufferConsumer.accept(buf);
        };
        CWidgetUpdate packet = new CWidgetUpdate(buffer);
        Minecraft.getMinecraft().player.connection.sendPacket(packet);


    }

    public void sendServerPacket(int discriminator, ISyncedWidget syncedWidget, ModularWindow window, Consumer<PacketBuffer> bufferConsumer) {
        if (player instanceof EntityPlayerMP) {
            int syncId = window.getSyncedWidgetId(syncedWidget);
            Consumer<PacketBuffer> buffer = buf -> {
                buf.writeVarInt(syncId);
                buf.writeVarInt(discriminator);
                bufferConsumer.accept(buf);
            };
            SWidgetUpdate packet = new SWidgetUpdate(buffer);
            ((EntityPlayerMP) player).connection.sendPacket(packet);
        }
    }
}
