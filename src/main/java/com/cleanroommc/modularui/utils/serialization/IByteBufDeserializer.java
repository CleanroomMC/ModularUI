package com.cleanroommc.modularui.utils.serialization;

import com.cleanroommc.modularui.ModularUI;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

/**
 * A function to read an object from a {@link PacketBuffer}.
 *
 * @param <T> object type
 */
public interface IByteBufDeserializer<T> {

    /**
     * Reads the object from the buffer.
     *
     * @param buffer buffer to read from
     * @return the read object
     * @throws IOException if reading failed
     */
    T deserialize(PacketBuffer buffer) throws IOException;

    default T deserializeSafe(PacketBuffer buffer) {
        try {
            return deserialize(buffer);
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
            return null;
        }
    }

    default IByteBufDeserializer<T> wrapNullSafe() {
        return buffer -> buffer.readBoolean() ? null : deserialize(buffer);
    }
}
