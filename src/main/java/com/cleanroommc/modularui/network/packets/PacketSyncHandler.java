package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.inventory.Container;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class PacketSyncHandler implements IPacket {

    private String panel;
    private String key;
    private FriendlyByteBuf packet;

    public PacketSyncHandler() {
    }

    public PacketSyncHandler(String panel, String key, FriendlyByteBuf packet) {
        this.panel = panel;
        this.key = key;
        this.packet = packet;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        NetworkUtils.writeStringSafe(buf, this.panel);
        NetworkUtils.writeStringSafe(buf, this.key, 64, true);
        NetworkUtils.writeByteBuf(buf, this.packet);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.panel = NetworkUtils.readStringSafe(buf);
        this.key = NetworkUtils.readStringSafe(buf);
        this.packet = NetworkUtils.readPacketBuffer(buf);
    }

    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        ModularScreen screen = ModularScreen.getCurrent();
        if (screen != null) {
            try {
                screen.getSyncManager().receiveWidgetUpdate(this.panel, this.key, this.packet.readVarInt(), this.packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        Container container = handler.player.openContainer;
        if (container instanceof ModularContainer modularContainer) {
            try {
                modularContainer.getSyncManager().receiveWidgetUpdate(this.panel, this.key, this.packet.readVarInt(), this.packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
