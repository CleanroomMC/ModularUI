package com.cleanroommc.modularui.common.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class NetworkUtils {

    public static void writePacketBuffer(PacketBuffer writeTo, PacketBuffer writeFrom) {
        writeTo.writeVarInt(writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom);
    }

    public static PacketBuffer readPacketBuffer(PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        return new PacketBuffer(copiedDataBuffer);
    }
}
