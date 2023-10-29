package com.cleanroommc.modularui.utils.serialization;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public interface IByteBufDeserializer<T> {

    T deserialize(PacketBuffer buffer) throws IOException;
}
