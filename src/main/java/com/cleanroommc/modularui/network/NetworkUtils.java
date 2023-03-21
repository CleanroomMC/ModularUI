package com.cleanroommc.modularui.network;

import com.cleanroommc.modularui.ModularUI;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class NetworkUtils {

    public static final Consumer<PacketBuffer> EMPTY_PACKET = buffer -> {
    };

    public static boolean isDedicatedClient() {
        return FMLCommonHandler.instance().getSide().isClient();
    }

    public static boolean isClient(EntityPlayer player) {
        if (player == null) throw new NullPointerException("Can't get side of null player!");
        return player.world == null ? player instanceof EntityPlayerSP : player.world.isRemote;
    }

    public static void writePacketBuffer(PacketBuffer writeTo, PacketBuffer writeFrom) {
        writeTo.writeVarInt(writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom);
    }

    public static PacketBuffer readPacketBuffer(PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        return new PacketBuffer(copiedDataBuffer);
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
        byte[] bytesTest = string == null ? new byte[0] : string.getBytes(StandardCharsets.UTF_8);
        byte[] bytes;

        if (bytesTest.length > 32767) {
            bytes = new byte[32767];
            System.arraycopy(bytesTest, 0, bytes, 0, 32767);
            ModularUI.LOGGER.warn("Warning! Synced string exceeds max length!");
        } else {
            bytes = bytesTest;
        }
        buffer.writeVarInt(bytes.length);
        buffer.writeBytes(bytes);
    }
}
