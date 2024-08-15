package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TileEntityGuiFactory extends AbstractUIFactory<PosGuiData> {

    public static final TileEntityGuiFactory INSTANCE = new TileEntityGuiFactory();

    private TileEntityGuiFactory() {
        super("mui:tile");
    }

    public static <T extends BlockEntity & IGuiHolder<PosGuiData>> void open(Player player, T tile) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(tile);
        if (tile.isRemoved()) {
            throw new IllegalArgumentException("Can't open invalid TileEntity GUI!");
        }
        if (player.level() != tile.getLevel()) {
            throw new IllegalArgumentException("TileEntity must be in same dimension as the player!");
        }
        BlockPos pos = tile.getBlockPos();
        PosGuiData data = new PosGuiData(player, pos.getX(), pos.getY(), pos.getZ());
        GuiManager.open(INSTANCE, data, (ServerPlayer) player);
    }

    public static void open(Player player, BlockPos pos) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(pos);
        PosGuiData data = new PosGuiData(player, pos.getX(), pos.getY(), pos.getZ());
        GuiManager.open(INSTANCE, data, (ServerPlayer) player);
    }

    @Override
    public @NotNull IGuiHolder<PosGuiData> getGuiHolder(PosGuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getTileEntity()), "Found TileEntity is not a gui holder!");
    }

    @Override
    public void writeGuiData(PosGuiData guiData, FriendlyByteBuf buffer) {
        buffer.writeVarInt(guiData.getX());
        buffer.writeVarInt(guiData.getY());
        buffer.writeVarInt(guiData.getZ());
    }

    @Override
    public @NotNull PosGuiData readGuiData(Player player, FriendlyByteBuf buffer) {
        return new PosGuiData(player, buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }
}
