package com.cleanroommc.modularui.utils.serialization;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public interface IByteBufSerializer<T> {

    void serialize(PacketBuffer buffer, T value) throws IOException;
}
