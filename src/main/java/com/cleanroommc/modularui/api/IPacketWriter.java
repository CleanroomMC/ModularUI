package com.cleanroommc.modularui.api;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

/**
 * A function that can write any data to an {@link PacketBuffer}.
 */
public interface IPacketWriter {

    /**
     * Writes any data to a packet buffer
     *
     * @param buffer buffer to write to
     * @throws IOException if data can not be written for some reason
     */
    void write(PacketBuffer buffer) throws IOException;
}
