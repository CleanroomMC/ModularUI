package com.cleanroommc.modularui.network;

import com.cleanroommc.modularui.ModularUI;
import com.google.common.base.Charsets;
import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
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
        return player.worldObj == null ? player instanceof EntityPlayerSP : player.worldObj.isRemote;
    }

    public static void writePacketBuffer(PacketBuffer writeTo, PacketBuffer writeFrom) {
        writeTo.writeVarIntToBuffer(writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom);
    }

    public static PacketBuffer readPacketBuffer(PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarIntFromBuffer());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        return new PacketBuffer(copiedDataBuffer);
    }

    public static void writeItemStack(PacketBuffer buffer, ItemStack itemStack) {
        try {
            buffer.writeItemStackToBuffer(itemStack);
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
        }
    }

    public static ItemStack readItemStack(PacketBuffer buffer) {
        try {
            return buffer.readItemStackFromBuffer();
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
            return null;
        }
    }

    public static void writeFluidStack(PacketBuffer buffer, @Nullable FluidStack fluidStack) {
        if (fluidStack == null) {
            buffer.writeBoolean(true);
        } else {
            buffer.writeBoolean(false);
            NBTTagCompound fluidStackTag = fluidStack.writeToNBT(new NBTTagCompound());
            try {
                buffer.writeNBTTagCompoundToBuffer(fluidStackTag);
            } catch (IOException e) {
                ModularUI.LOGGER.catching(e);
            }
        }
    }

    @Nullable
    public static FluidStack readFluidStack(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            return null;
        }
        try {
            return FluidStack.loadFluidStackFromNBT(buffer.readNBTTagCompoundFromBuffer());
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
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
        buffer.writeVarIntToBuffer(bytes.length);
        buffer.writeBytes(bytes);
    }

    public static String readStringSafe(PacketBuffer buffer, int maxLength) {
        int length = buffer.readVarIntFromBuffer();

        if (length > maxLength * 4) {
            ModularUI.LOGGER.warn("Warning! Received string exceeds max length!");
        }
        String string = new String(buffer.readBytes(Math.min(length, maxLength * 4)).array(), Charsets.UTF_8);
        if (string.length() > maxLength) {
            return string.substring(0, maxLength);
        } else {
            return string;
        }
    }

    public static void writeEnumValue(PacketBuffer buffer, Enum<?> value) {
        buffer.writeVarIntToBuffer(value.ordinal());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T readEnumValue(PacketBuffer buffer, Class<T> enumClass) {
        return (T)((Enum<T>[])enumClass.getEnumConstants())[buffer.readVarIntFromBuffer()];
    }
}
