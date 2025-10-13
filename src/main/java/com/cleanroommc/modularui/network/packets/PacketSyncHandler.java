package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;

import com.cleanroommc.modularui.value.sync.ModularSyncManager;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.inventory.Container;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class PacketSyncHandler implements IPacket {

    private String panel;
    private String key;
    private boolean action;
    private PacketBuffer packet;

    public PacketSyncHandler() {}

    public PacketSyncHandler(String panel, String key, boolean action, PacketBuffer packet) {
        this.panel = panel;
        this.key = key;
        this.action = action;
        this.packet = packet;
    }

    @Override
    public void write(PacketBuffer buf) {
        NetworkUtils.writeStringSafe(buf, this.panel);
        NetworkUtils.writeStringSafe(buf, this.key, 64, true);
        buf.writeBoolean(this.action);
        NetworkUtils.writeByteBuf(buf, this.packet);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.panel = NetworkUtils.readStringSafe(buf);
        this.key = NetworkUtils.readStringSafe(buf);
        this.action = buf.readBoolean();
        this.packet = NetworkUtils.readPacketBuffer(buf);
    }

    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        ModularScreen screen = ModularScreen.getCurrent();
        if (screen != null) {
            execute(screen.getSyncManager());
        }
        return null;
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        Container container = handler.player.openContainer;
        if (container instanceof ModularContainer modularContainer) {
            execute(modularContainer.getSyncManager());
        }
        return null;
    }

    private void execute(ModularSyncManager syncManager) {
        try {
            int id = this.action ? 0 : this.packet.readVarInt();
            syncManager.receiveWidgetUpdate(this.panel, this.key, this.action, id, this.packet);
        } catch (IndexOutOfBoundsException e) {
            ModularUI.LOGGER.error("Failed to read packet for sync handler {} in panel {}", this.key, this.panel);
        } catch (IOException e) {
            ModularUI.LOGGER.throwing(e);
        }
    }
}
