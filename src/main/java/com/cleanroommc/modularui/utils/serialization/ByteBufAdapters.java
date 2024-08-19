package com.cleanroommc.modularui.utils.serialization;

import com.cleanroommc.modularui.network.NetworkUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ByteBufAdapters {

    public static final IByteBufAdapter<ItemStack> ITEM_STACK = makeAdapter(FriendlyByteBuf::readItem, FriendlyByteBuf::writeItem, ItemStack::isSameItem);
    public static final IByteBufAdapter<FluidStack> FLUID_STACK = makeAdapter(NetworkUtils::readFluidStack, NetworkUtils::writeFluidStack, FluidStack::isFluidStackIdentical);
    public static final IByteBufAdapter<CompoundTag> NBT = makeAdapter(FriendlyByteBuf::readNbt, FriendlyByteBuf::writeNbt, null);
    public static final IByteBufAdapter<String> STRING = makeAdapter(NetworkUtils::readStringSafe, NetworkUtils::writeStringSafe, null);
    public static final IByteBufAdapter<ByteBuf> BYTE_BUF = makeAdapter(NetworkUtils::readByteBuf, NetworkUtils::writeByteBuf, null);
    public static final IByteBufAdapter<FriendlyByteBuf> PACKET_BUFFER = makeAdapter(NetworkUtils::readPacketBuffer, NetworkUtils::writeByteBuf, null);

    public static <T> IByteBufAdapter<T> makeAdapter(@NotNull IByteBufDeserializer<T> deserializer, @NotNull IByteBufSerializer<T> serializer, @Nullable IEquals<T> comparator) {
        final IEquals<T> tester = comparator != null ? comparator : IEquals.defaultTester();
        return new IByteBufAdapter<T>() {
            @Override
            public T deserialize(FriendlyByteBuf buffer) throws IOException {
                return deserializer.deserialize(buffer);
            }

            @Override
            public void serialize(FriendlyByteBuf buffer, T u) throws IOException {
                serializer.serialize(buffer, u);
            }

            @Override
            public boolean areEqual(@NotNull T t1, @NotNull T t2) {
                return tester.areEqual(t1, t2);
            }
        };
    }
}
