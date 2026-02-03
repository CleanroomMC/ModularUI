package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;

import net.minecraft.network.PacketBuffer;

import io.netty.buffer.Unpooled;

import java.io.IOException;

public interface ICopy<T> {

    static <T> ICopy<T> immutable() {
        return t -> t;
    }

    static <T> ICopy<T> ofSerializer(IByteBufSerializer<T> serializer, IByteBufDeserializer<T> deserializer) {
        return t -> {
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            try {
                serializer.serialize(buffer, t);
                return deserializer.deserialize(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    static <T> ICopy<T> ofSerializer(IByteBufAdapter<T> adapter) {
        return ofSerializer(adapter, adapter);
    }

    T createDeepCopy(T t);

    static <T> ICopy<T> wrapNullSafe(ICopy<T> copy) {
        return t -> t == null ? null : copy.createDeepCopy(t);
    }
}
