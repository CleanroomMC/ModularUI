package com.cleanroommc.modularui.api;

import io.netty.buffer.Unpooled;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

/**
 * A function that can write any data to an {@link PacketBuffer}.
 */
@FunctionalInterface
public interface IPacketWriter {

    /**
     * Writes any data to a packet buffer
     *
     * @param buffer buffer to write to
     * @throws IOException if data can not be written for some reason
     */
    void write(PacketBuffer buffer) throws IOException;

    default PacketBuffer toPacket() {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        try {
            write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }
}
