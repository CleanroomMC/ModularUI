package com.cleanroommc.modularui.common.internal.network;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.screen.ModularUIContext;
import com.cleanroommc.modularui.common.internal.wrapper.ModularUIContainer;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class CWidgetUpdate implements IPacket {

    public int widgetId;
    public PacketBuffer packet;

    public CWidgetUpdate(PacketBuffer packet, int widgetId) {
        this.packet = packet;
        this.widgetId = widgetId;
    }

    public CWidgetUpdate() {
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
    public IPacket executeServer(NetHandlerPlayServer handler) {
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
        return null;
    }
}
