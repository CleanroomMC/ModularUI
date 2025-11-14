package com.cleanroommc.modularui.utils.serialization;

import com.cleanroommc.modularui.network.NetworkUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class ByteBufAdapters {

    public static final IByteBufAdapter<ItemStack> ITEM_STACK = makeAdapter(PacketBuffer::readItemStack, PacketBuffer::writeItemStack, ItemStack::areItemStacksEqual);
    public static final IByteBufAdapter<FluidStack> FLUID_STACK = makeAdapter(NetworkUtils::readFluidStack, NetworkUtils::writeFluidStack, FluidStack::isFluidStackIdentical);
    public static final IByteBufAdapter<NBTTagCompound> NBT = makeAdapter(PacketBuffer::readCompoundTag, PacketBuffer::writeCompoundTag, null);
    public static final IByteBufAdapter<String> STRING = makeAdapter(NetworkUtils::readStringSafe, NetworkUtils::writeStringSafe, null);
    public static final IByteBufAdapter<ByteBuf> BYTE_BUF = makeAdapter(NetworkUtils::readByteBuf, NetworkUtils::writeByteBuf, null);
    public static final IByteBufAdapter<PacketBuffer> PACKET_BUFFER = makeAdapter(NetworkUtils::readPacketBuffer, NetworkUtils::writeByteBuf, null);

    public static final IByteBufAdapter<byte[]> BYTE_ARR = new IByteBufAdapter<>() {
        @Override
        public byte[] deserialize(PacketBuffer buffer) throws IOException {
            return buffer.readByteArray();
        }

        @Override
        public void serialize(PacketBuffer buffer, byte[] u) throws IOException {
            buffer.writeByteArray(u);
        }

        @Override
        public boolean areEqual(byte @NotNull [] t1, byte @NotNull [] t2) {
            if (t1.length != t2.length) return false;
            for (int i = 0; i < t1.length; i++) {
                if (t1[i] != t2[i]) return false;
            }
            return true;
        }
    };

    public static final IByteBufAdapter<long[]> LONG_ARR = new IByteBufAdapter<>() {
        @Override
        public long[] deserialize(PacketBuffer buffer) throws IOException {
            long[] u = new long[buffer.readVarInt()];
            for (int i = 0; i < u.length; i++) {
                u[i] = buffer.readLong();
            }
            return u;
        }

        @Override
        public void serialize(PacketBuffer buffer, long[] u) throws IOException {
            buffer.writeVarInt(u.length);
            for (long i : u) {
                buffer.writeLong(i);
            }
        }

        @Override
        public boolean areEqual(long @NotNull [] t1, long @NotNull [] t2) {
            if (t1.length != t2.length) return false;
            for (int i = 0; i < t1.length; i++) {
                if (t1[i] != t2[i]) return false;
            }
            return true;
        }
    };

    public static final IByteBufAdapter<BigInteger> BIG_INT = new IByteBufAdapter<>() {
        @Override
        public BigInteger deserialize(PacketBuffer buffer) throws IOException {
            return new BigInteger(buffer.readByteArray());
        }

        @Override
        public void serialize(PacketBuffer buffer, BigInteger u) throws IOException {
            buffer.writeBytes(u.toByteArray());
        }

        @Override
        public boolean areEqual(@NotNull BigInteger t1, @NotNull BigInteger t2) {
            return t1.equals(t2);
        }
    };

    public static final IByteBufAdapter<BigDecimal> BIG_DECIMAL = new IByteBufAdapter<>() {
        @Override
        public BigDecimal deserialize(PacketBuffer buffer) throws IOException {
            return new BigDecimal(BIG_INT.deserialize(buffer), buffer.readVarInt());
        }

        @Override
        public void serialize(PacketBuffer buffer, BigDecimal u) throws IOException {
            BIG_INT.serialize(buffer, u.unscaledValue());
            buffer.writeVarInt(u.scale());
        }

        @Override
        public boolean areEqual(@NotNull BigDecimal t1, @NotNull BigDecimal t2) {
            return t1.equals(t2);
        }
    };

    public static <T> IByteBufAdapter<T> makeAdapter(@NotNull IByteBufDeserializer<T> deserializer, @NotNull IByteBufSerializer<T> serializer, @Nullable IEquals<T> comparator) {
        final IEquals<T> tester = comparator != null ? comparator : IEquals.defaultTester();
        return new IByteBufAdapter<>() {
            @Override
            public T deserialize(PacketBuffer buffer) throws IOException {
                return deserializer.deserialize(buffer);
            }

            @Override
            public void serialize(PacketBuffer buffer, T u) throws IOException {
                serializer.serialize(buffer, u);
            }

            @Override
            public boolean areEqual(@NotNull T t1, @NotNull T t2) {
                return tester.areEqual(t1, t2);
            }
        };
    }
}
