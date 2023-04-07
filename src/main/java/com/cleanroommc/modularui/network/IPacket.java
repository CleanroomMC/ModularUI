package com.cleanroommc.modularui.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.Nullable;

public interface IPacket extends IMessage {

    /**
     * Write this packet to the buffer
     *
     * @param buf buffer to write to
     */
    void write(PacketBuffer buf);

    /**
     * Read this packet from the buffer.
     * Do not do anything else other than reading this packet!
     *
     * @param buf buffer to read from
     */
    void read(PacketBuffer buf);

    /**
     * Called when packet is sent from server to client.
     *
     * @param handler network handler
     * @return response packet
     */
    @SideOnly(Side.CLIENT)
    @Nullable
    default IPacket executeClient(NetHandlerPlayClient handler) {
        return null;
    }

    /**
     * Called when packet is sent from client to server.
     *
     * @param handler network handler
     * @return response packet
     */
    @Nullable
    default IPacket executeServer(NetHandlerPlayServer handler) {
        return null;
    }

    @Deprecated
    @Override
    default void fromBytes(ByteBuf buf) {
        read(buf instanceof PacketBuffer ? (PacketBuffer) buf : new PacketBuffer(buf));
    }

    @Deprecated
    @Override
    default void toBytes(ByteBuf buf) {
        write(buf instanceof PacketBuffer ? (PacketBuffer) buf : new PacketBuffer(buf));
    }
}
