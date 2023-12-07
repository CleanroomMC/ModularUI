package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SidedTileEntityGuiFactory extends AbstractUIFactory<SidedPosGuiData> {

    public static final SidedTileEntityGuiFactory INSTANCE = new SidedTileEntityGuiFactory();

    public static <T extends TileEntity & IGuiHolder<SidedPosGuiData>> void open(EntityPlayer player, T tile, EnumFacing facing) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(tile);
        Objects.requireNonNull(facing);
        if (tile.isInvalid()) {
            throw new IllegalArgumentException("Can't open invalid TileEntity GUI!");
        }
        if (player.world != tile.getWorld()) {
            throw new IllegalArgumentException("TileEntity must be in same dimension as the player!");
        }
        BlockPos pos = tile.getPos();
        SidedPosGuiData data = new SidedPosGuiData(player, pos.getX(), pos.getY(), pos.getZ(), facing);
        GuiManager.open(INSTANCE, data, (EntityPlayerMP) player);
    }

    public static void open(EntityPlayer player, BlockPos pos, EnumFacing facing) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(pos);
        Objects.requireNonNull(facing);
        SidedPosGuiData data = new SidedPosGuiData(player, pos.getX(), pos.getY(), pos.getZ(), facing);
        GuiManager.open(INSTANCE, data, (EntityPlayerMP) player);
    }

    private SidedTileEntityGuiFactory() {
        super("mui:sided_tile");
    }

    @Override
    public @NotNull IGuiHolder<SidedPosGuiData> getGuiHolder(SidedPosGuiData data) {
        TileEntity te = data.getTileEntity();
        if (isGuiHolder(te)) {
            return (IGuiHolder<SidedPosGuiData>) te;
        }
        throw new IllegalStateException("Found TileEntity is not a valid gui holder!");
    }

    @Override
    public void writeGuiData(SidedPosGuiData guiData, PacketBuffer buffer) {
        buffer.writeVarInt(guiData.getX());
        buffer.writeVarInt(guiData.getY());
        buffer.writeVarInt(guiData.getZ());
        buffer.writeByte(guiData.getSide().getIndex());
    }

    @Override
    public @NotNull SidedPosGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new SidedPosGuiData(player, buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), EnumFacing.VALUES[buffer.readByte()]);
    }
}
