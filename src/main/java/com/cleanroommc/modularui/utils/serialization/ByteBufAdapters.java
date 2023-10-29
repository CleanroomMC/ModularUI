package com.cleanroommc.modularui.utils.serialization;

import com.cleanroommc.modularui.network.NetworkUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ByteBufAdapters {

    public static final IByteBufAdapter<ItemStack> ITEM_STACK = makeAdapter(NetworkUtils::readItemStack, NetworkUtils::writeItemStack, ItemStack::areItemStacksEqual);
    public static final IByteBufAdapter<FluidStack> FLUID_STACK = makeAdapter(NetworkUtils::readFluidStack, NetworkUtils::writeFluidStack, FluidStack::isFluidStackIdentical);
    public static final IByteBufAdapter<NBTTagCompound> NBT = makeAdapter(PacketBuffer::readCompoundTag, PacketBuffer::writeCompoundTag, null);
    public static final IByteBufAdapter<String> STRING = makeAdapter(buf -> buf.readString(Short.MAX_VALUE), NetworkUtils::writeStringSafe, null);

    public static <T> IByteBufAdapter<T> makeAdapter(@NotNull IByteBufDeserializer<T> deserializer, @NotNull IByteBufSerializer<T> serializer, @Nullable IEquals<T> comparator) {
        final IEquals<T> tester = comparator != null ? comparator : IEquals.defaultTester();
        return new IByteBufAdapter<T>() {
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
