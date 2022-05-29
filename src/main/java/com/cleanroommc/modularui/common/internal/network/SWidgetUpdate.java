package com.cleanroommc.modularui.common.internal.network;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.screen.ModularUIContext;
import com.cleanroommc.modularui.common.internal.wrapper.ModularGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class SWidgetUpdate implements IPacket {

    public int widgetId;
    public PacketBuffer packet;

    public SWidgetUpdate(PacketBuffer packet, int widgetId) {
        this.packet = packet;
        this.widgetId = widgetId;
    }

    public SWidgetUpdate() {
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.widgetId = buf.readVarInt();
        this.packet = NetworkUtils.readPacketBuffer(buf);
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(widgetId);
        NetworkUtils.writePacketBuffer(buf, packet);
    }

    @Override
    public IPacket executeClient(NetHandlerPlayClient handler) {
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
        return null;
    }
}
