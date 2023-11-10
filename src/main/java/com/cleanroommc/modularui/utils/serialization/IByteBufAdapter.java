package com.cleanroommc.modularui.utils.serialization;

import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface IByteBufAdapter<T> extends IByteBufSerializer<T>, IByteBufDeserializer<T>, IEquals<T> {

    @Override
    T deserialize(PacketBuffer buffer) throws IOException;

    @Override
    void serialize(PacketBuffer buffer, T u) throws IOException;

    @Override
    boolean areEqual(@NotNull T t1, @NotNull T t2);
}
