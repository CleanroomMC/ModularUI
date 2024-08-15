package com.cleanroommc.modularui.network;

import com.cleanroommc.modularui.ModularUI;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidStack;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraftforge.fml.loading.FMLEnvironment;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class NetworkUtils {

    public static final Consumer<FriendlyByteBuf> EMPTY_PACKET = buffer -> {
    };

    public static final boolean DEDICATED_CLIENT = FMLEnvironment.dist.isClient();

    public static boolean isClient() {
        return FMLEnvironment.dist.isClient() && Minecraft.getInstance().isSameThread();
    }

    public static boolean isDedicatedClient() {
        return DEDICATED_CLIENT;
    }

    public static boolean isClient(Player player) {
        if (player == null) throw new NullPointerException("Can't get side of null player!");
        return player.level() == null ? !(player instanceof ServerPlayer) : player.level().isClientSide;
    }

    public static void writeByteBuf(FriendlyByteBuf writeTo, ByteBuf writeFrom) {
        writeTo.writeVarInt(writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom);
    }

    public static ByteBuf readByteBuf(FriendlyByteBuf buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        return copiedDataBuffer;
    }

    public static FriendlyByteBuf readPacketBuffer(FriendlyByteBuf buf) {
        return new FriendlyByteBuf(readByteBuf(buf));
    }

    public static void writeItemStack(FriendlyByteBuf buffer, ItemStack itemStack) {
        buffer.writeItemStack(itemStack, false);
    }

    public static ItemStack readItemStack(FriendlyByteBuf buffer) {
            return buffer.readItem();
    }

    public static void writeFluidStack(FriendlyByteBuf buffer, @Nullable FluidStack fluidStack) {
        if (fluidStack == null) {
            buffer.writeBoolean(true);
        } else {
            buffer.writeBoolean(false);
            CompoundTag fluidStackTag = fluidStack.writeToNBT(new CompoundTag());
            buffer.writeNbt(fluidStackTag);
        }
    }

    @Nullable
    public static FluidStack readFluidStack(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(buffer.readNbt());
    }

    public static void writeStringSafe(FriendlyByteBuf buffer, String string) {
        writeStringSafe(buffer, string, Short.MAX_VALUE, false);
    }

    public static void writeStringSafe(FriendlyByteBuf buffer, @Nullable String string, boolean crash) {
        writeStringSafe(buffer, string, Short.MAX_VALUE, crash);
    }

    public static void writeStringSafe(FriendlyByteBuf buffer, @Nullable String string, int maxBytes) {
        writeStringSafe(buffer, string, maxBytes, false);
    }

    public static void writeStringSafe(FriendlyByteBuf buffer, @Nullable String string, int maxBytes, boolean crash) {
        maxBytes = Math.min(maxBytes, Short.MAX_VALUE);
        if (string == null) {
            buffer.writeVarInt(Short.MAX_VALUE + 1);
            return;
        }
        byte[] bytesTest = string.getBytes(StandardCharsets.UTF_8);
        byte[] bytes;

        if (bytesTest.length > maxBytes) {
            if (crash) {
                throw new IllegalArgumentException("Max String size is " + maxBytes + ", but found " + bytesTest.length + " bytes for '" + string + "'!");
            }
            bytes = new byte[maxBytes];
            System.arraycopy(bytesTest, 0, bytes, 0, maxBytes);
            ModularUI.LOGGER.warn("Warning! Synced string exceeds max length!");
        } else {
            bytes = bytesTest;
        }
        buffer.writeVarInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    public static String readStringSafe(FriendlyByteBuf buffer) {
        int length = buffer.readVarInt();
        if (length > Short.MAX_VALUE) {
            return null;
        }
        String s = buffer.toString(buffer.readerIndex(), length, StandardCharsets.UTF_8);
        buffer.readerIndex(buffer.readerIndex() + length);
        return s;
    }
}
