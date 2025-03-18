package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface IReadablePacket {

    int peekInt();
    long peekLong();
    int peekVarInt();
    long peekVarLong();
    float peekFloat();
    char peekChar();
    String peekString();
    double peekDouble();
    boolean peekBoolean();
    byte peekByte();
    byte[] peekBytes();
    short peekShort();
    ItemStack peekItem();
    FluidStack peekFluid();
    PacketBuffer peekPacketBuffer();
    <T> T peek(IByteBufDeserializer<T> deserializer);
    <T> List<T> peekList(Supplier<List<T>> listSupplier, IByteBufDeserializer<T> deserializer);
    default <T> List<T> peekList(IByteBufDeserializer<T> deserializer) {
        return peekList(ArrayList::new, deserializer);
    }
}
