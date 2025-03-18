package com.cleanroommc.modularui.api;

import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IWritablePacket {

    IWritablePacket putInt(int value);
    IWritablePacket putLong(long value);
    IWritablePacket putVarInt(int value);
    IWritablePacket putVarLong(long value);
    IWritablePacket putString(String value);
    IWritablePacket putBoolean(boolean value);
    IWritablePacket putFloat(float value);
    IWritablePacket putDouble(double value);
    IWritablePacket putByte(byte value);
    IWritablePacket putBytes(byte[] value);
    IWritablePacket putChar(char value);
    IWritablePacket putItem(ItemStack value);
    IWritablePacket putFluid(FluidStack value);
    IWritablePacket putBlockPos(BlockPos value);
    IWritablePacket putPacket(ByteBuf buffer);
    <T> IWritablePacket put(T value, IByteBufSerializer<T> serializer);
    <T> IWritablePacket putList(List<T> value, IByteBufSerializer<T> serializer);
}
