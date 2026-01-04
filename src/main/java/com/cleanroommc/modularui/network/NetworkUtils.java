package com.cleanroommc.modularui.network;

import com.cleanroommc.modularui.ModularUI;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class NetworkUtils {

    public static final Consumer<PacketBuffer> EMPTY_PACKET = buffer -> {};

    public static final boolean DEDICATED_CLIENT = FMLCommonHandler.instance().getSide().isClient();

    public static boolean isClient() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    public static boolean isDedicatedClient() {
        return DEDICATED_CLIENT;
    }

    public static boolean isClient(EntityPlayer player) {
        if (player == null) return isClient();
        return player.world == null ? player instanceof EntityPlayerSP : player.world.isRemote;
    }

    public static void writeByteBuf(PacketBuffer writeTo, ByteBuf writeFrom) {
        writeTo.writeVarInt(writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom);
    }

    public static ByteBuf readByteBuf(PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        return copiedDataBuffer;
    }

    public static PacketBuffer readPacketBuffer(PacketBuffer buf) {
        return new PacketBuffer(readByteBuf(buf));
    }

    public static void writeItemStack(PacketBuffer buffer, ItemStack itemStack) {
        buffer.writeItemStack(itemStack);
    }

    public static ItemStack readItemStack(PacketBuffer buffer) {
        try {
            return buffer.readItemStack();
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
            return ItemStack.EMPTY;
        }
    }

    public static void writeFluidStack(PacketBuffer buffer, @Nullable FluidStack fluidStack) {
        if (fluidStack == null) {
            buffer.writeBoolean(true);
        } else {
            buffer.writeBoolean(false);
            NBTTagCompound fluidStackTag = fluidStack.writeToNBT(new NBTTagCompound());
            buffer.writeCompoundTag(fluidStackTag);
        }
    }

    @Nullable
    public static FluidStack readFluidStack(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            return null;
        }
        try {
            return FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
        } catch (IOException e) {
            ModularUI.LOGGER.throwing(e);
            return null;
        }
    }

    public static void writeStringSafe(PacketBuffer buffer, String string) {
        writeStringSafe(buffer, string, Short.MAX_VALUE, false);
    }

    public static void writeStringSafe(PacketBuffer buffer, @Nullable String string, boolean crash) {
        writeStringSafe(buffer, string, Short.MAX_VALUE, crash);
    }

    public static void writeStringSafe(PacketBuffer buffer, @Nullable String string, int maxBytes) {
        writeStringSafe(buffer, string, maxBytes, false);
    }

    public static void writeStringSafe(PacketBuffer buffer, @Nullable String string, int maxBytes, boolean crash) {
        maxBytes = Math.min(maxBytes, Short.MAX_VALUE);
        if (string == null) {
            buffer.writeVarInt(Short.MAX_VALUE + 1);
            return;
        }
        if (string.isEmpty()) {
            buffer.writeVarInt(0);
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

    public static String readStringSafe(PacketBuffer buffer) {
        int length = buffer.readVarInt();
        if (length > Short.MAX_VALUE) return null;
        if (length == 0) return StringUtils.EMPTY;
        String s = buffer.toString(buffer.readerIndex(), length, StandardCharsets.UTF_8);
        buffer.readerIndex(buffer.readerIndex() + length);
        return s;
    }
}
