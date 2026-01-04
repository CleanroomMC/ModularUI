package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.network.ModularNetwork;
import com.cleanroommc.modularui.network.NetworkUtils;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class PacketSyncHandler implements IPacket {

    public int networkId;
    public String panel;
    public String key;
    public boolean action;
    public PacketBuffer packet;

    public PacketSyncHandler() {}

    public PacketSyncHandler(int networkId, String panel, String key, boolean action, PacketBuffer packet) {
        this.networkId = networkId;
        this.panel = panel;
        this.key = key;
        this.action = action;
        this.packet = packet;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeVarInt(this.networkId);
        NetworkUtils.writeStringSafe(buf, this.panel, 256, true);
        NetworkUtils.writeStringSafe(buf, this.key, 256, true);
        buf.writeBoolean(this.action);
        NetworkUtils.writeByteBuf(buf, this.packet);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.networkId = buf.readVarInt();
        this.panel = NetworkUtils.readStringSafe(buf);
        this.key = NetworkUtils.readStringSafe(buf);
        this.action = buf.readBoolean();
        this.packet = NetworkUtils.readPacketBuffer(buf);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @Nullable IPacket executeClient(NetHandlerPlayClient handler) {
        ModularNetwork.CLIENT.receivePacket(this);
        return null;
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        ModularNetwork.SERVER.receivePacket(this);
        return null;
    }
}
