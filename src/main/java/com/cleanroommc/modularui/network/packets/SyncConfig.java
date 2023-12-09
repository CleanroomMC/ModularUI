package com.cleanroommc.modularui.network.packets;

import com.cleanroommc.modularui.config.Config;
import com.cleanroommc.modularui.network.IPacket;
import com.cleanroommc.modularui.network.NetworkUtils;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;

public class SyncConfig implements IPacket {

    private String name;
    private PacketBuffer buffer;

    public SyncConfig() {
    }

    public SyncConfig(Config config) {
        this.name = config.getName();
        this.buffer = new PacketBuffer(Unpooled.buffer());
        config.writeToBuffer(this.buffer);
    }

    @Override
    public void write(PacketBuffer buf) {
        NetworkUtils.writeStringSafe(buf, this.name);
        NetworkUtils.writeByteBuf(buf, this.buffer);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.name = NetworkUtils.readStringSafe(buf);
        this.buffer = NetworkUtils.readPacketBuffer(buf);
    }

    @Override
    public @Nullable IPacket executeServer(NetHandlerPlayServer handler) {
        Config config = Config.getConfig(this.name);
        config.readFromBuffer(this.buffer);
        return null;
    }
}
