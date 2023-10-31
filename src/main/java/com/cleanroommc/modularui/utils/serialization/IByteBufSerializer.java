package com.cleanroommc.modularui.utils.serialization;

import com.cleanroommc.modularui.ModularUI;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

/**
 * A function that writes an object to a {@link PacketBuffer}.
 *
 * @param <T> object type
 */
public interface IByteBufSerializer<T> {

    /**
     * Writes the object to the buffer.
     *
     * @param buffer buffer to write to
     * @param value  object to write
     * @throws IOException if writing failed
     */
    void serialize(PacketBuffer buffer, T value) throws IOException;

    default void serializeSafe(PacketBuffer buffer, T value) {
        try {
            serialize(buffer, value);
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
        }
    }
}
