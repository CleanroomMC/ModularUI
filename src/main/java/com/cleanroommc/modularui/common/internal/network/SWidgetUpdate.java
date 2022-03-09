package com.cleanroommc.modularui.common.internal.network;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class SWidgetUpdate implements Packet<INetHandlerPlayClient> {

    public int widgetId;
    public PacketBuffer packet;

    public SWidgetUpdate(PacketBuffer packet, int widgetId) {
        this.packet = packet;
        this.widgetId = widgetId;
    }

    public SWidgetUpdate() {
    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.widgetId = buf.readVarInt();
        this.packet = NetworkUtils.readPacketBuffer(buf);
    }

    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeVarInt(widgetId);
        NetworkUtils.writePacketBuffer(buf, packet);
    }

    @Override
    public void processPacket(INetHandlerPlayClient handler) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof ModularGui) {
            ModularUIContext context = ((ModularGui) screen).getContext();
            try {
                context.readServerPacket(packet, widgetId);
            } catch (IOException e) {
                ModularUI.LOGGER.error("Error reading server packet: ");
                e.printStackTrace();
            }
        } else {
            ModularUI.LOGGER.error("Expected ModularGui screen on client, but got {}", screen);
        }
    }
}
