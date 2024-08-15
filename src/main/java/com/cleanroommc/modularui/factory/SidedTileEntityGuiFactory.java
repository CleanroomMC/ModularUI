package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SidedTileEntityGuiFactory extends AbstractUIFactory<SidedPosGuiData> {

    public static final SidedTileEntityGuiFactory INSTANCE = new SidedTileEntityGuiFactory();

    public static <T extends BlockEntity & IGuiHolder<SidedPosGuiData>> void open(Player player, T tile, Direction facing) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(tile);
        Objects.requireNonNull(facing);
        if (tile.isRemoved()) {
            throw new IllegalArgumentException("Can't open invalid TileEntity GUI!");
        }
        if (player.level() != tile.getLevel()) {
            throw new IllegalArgumentException("TileEntity must be in same dimension as the player!");
        }
        BlockPos pos = tile.getBlockPos();
        SidedPosGuiData data = new SidedPosGuiData(player, pos.getX(), pos.getY(), pos.getZ(), facing);
        GuiManager.open(INSTANCE, data, (ServerPlayer) player);
    }

    public static void open(Player player, BlockPos pos, Direction facing) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(pos);
        Objects.requireNonNull(facing);
        SidedPosGuiData data = new SidedPosGuiData(player, pos.getX(), pos.getY(), pos.getZ(), facing);
        GuiManager.open(INSTANCE, data, (ServerPlayer) player);
    }

    private SidedTileEntityGuiFactory() {
        super("mui:sided_tile");
    }

    @Override
    public @NotNull IGuiHolder<SidedPosGuiData> getGuiHolder(SidedPosGuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getTileEntity()), "Found TileEntity is not a gui holder!");
    }

    @Override
    public void writeGuiData(SidedPosGuiData guiData, FriendlyByteBuf buffer) {
        buffer.writeVarInt(guiData.getX());
        buffer.writeVarInt(guiData.getY());
        buffer.writeVarInt(guiData.getZ());
        buffer.writeByte(guiData.getSide().get3DDataValue());
    }

    @Override
    public @NotNull SidedPosGuiData readGuiData(Player player, FriendlyByteBuf buffer) {
        return new SidedPosGuiData(player, buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(), Direction.values()[buffer.readByte()]);
    }
}
