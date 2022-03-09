package com.cleanroommc.modularui.common.internal.network;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.common.internal.ModularUIContext;
import com.cleanroommc.modularui.common.internal.wrapper.ModularUIContainer;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class CWidgetUpdate implements Packet<NetHandlerPlayServer> {

    public int widgetId;
    public PacketBuffer packet;

    public CWidgetUpdate(PacketBuffer packet, int widgetId) {
        this.packet = packet;
        this.widgetId = widgetId;
    }

    public CWidgetUpdate() {
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
    public void processPacket(NetHandlerPlayServer handler) {
        Container container = handler.player.openContainer;
        if (container instanceof ModularUIContainer) {
            ModularUIContext context = ((ModularUIContainer) container).getContext();
            try {
                context.readClientPacket(packet, widgetId);
            } catch (IOException e) {
                ModularUI.LOGGER.error("Error reading client packet: ");
                e.printStackTrace();
            }
        } else {
            ModularUI.LOGGER.error("Expected ModularUIContainer on server, but got {}", container);
        }
    }
}
