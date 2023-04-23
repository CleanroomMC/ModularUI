package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class PacketSyncHandler implements IPacket {

    private String key;
    private PacketBuffer packet;

    public PacketSyncHandler() {
    }

    public PacketSyncHandler(String key, PacketBuffer packet) {
        this.key = key;
        this.packet = packet;
    }

    @Override
    public void write(PacketBuffer buf) {
        NetworkUtils.writeStringSafe(buf, this.key);
        NetworkUtils.writePacketBuffer(buf, this.packet);
    }

    @Override
    public void read(PacketBuffer buf) {
        key = buf.readString(64);
        packet = NetworkUtils.readPacketBuffer(buf);
    }

    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        ModularScreen screen = ModularScreen.getCurrent();
        if (screen != null) {
            try {
                screen.getSyncHandler().receiveWidgetUpdate(this.key, this.packet.readVarInt(), this.packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        Container container = handler.player.openContainer;
        if (container instanceof ModularContainer) {
            try {
                ((ModularContainer) container).getSyncHandler().receiveWidgetUpdate(this.key, this.packet.readVarInt(), this.packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
