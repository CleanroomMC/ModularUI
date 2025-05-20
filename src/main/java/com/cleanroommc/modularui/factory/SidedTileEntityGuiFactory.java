package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.utils.Platform;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SidedTileEntityGuiFactory extends AbstractUIFactory<SidedPosGuiData> {

    public static final SidedTileEntityGuiFactory INSTANCE = new SidedTileEntityGuiFactory();

    public <T extends TileEntity & IGuiHolder<SidedPosGuiData>> void open(EntityPlayer player, T tile, EnumFacing facing) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(facing);
        TileEntityGuiFactory.verifyTile(player, tile);
        BlockPos pos = tile.getPos();
        SidedPosGuiData data = new SidedPosGuiData(player, pos.getX(), pos.getY(), pos.getZ(), facing);
        GuiManager.open(this, data, (EntityPlayerMP) player);
    }

    public void open(EntityPlayer player, BlockPos pos, EnumFacing facing) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(pos);
        Objects.requireNonNull(facing);
        SidedPosGuiData data = new SidedPosGuiData(player, pos.getX(), pos.getY(), pos.getZ(), facing);
        GuiManager.open(this, data, (EntityPlayerMP) player);
    }

    @SideOnly(Side.CLIENT)
    public <T extends TileEntity & IGuiHolder<SidedPosGuiData>> void openClient(T tile, EnumFacing facing) {
        Objects.requireNonNull(facing);
        TileEntityGuiFactory.verifyTile(Platform.getClientPlayer(), tile);
        BlockPos pos = tile.getPos();
        SidedPosGuiData data = new SidedPosGuiData(Platform.getClientPlayer(), pos.getX(), pos.getY(), pos.getZ(), facing);
        GuiManager.openFromClient(this, data);
    }

    @SideOnly(Side.CLIENT)
    public void openClient(BlockPos pos, EnumFacing facing) {
        Objects.requireNonNull(pos);
        Objects.requireNonNull(facing);
        SidedPosGuiData data = new SidedPosGuiData(Platform.getClientPlayer(), pos.getX(), pos.getY(), pos.getZ(), facing);
        GuiManager.openFromClient(this, data);
    }

    private SidedTileEntityGuiFactory() {
        super("mui:sided_tile");
    }

    @Override
    public @NotNull IGuiHolder<SidedPosGuiData> getGuiHolder(SidedPosGuiData data) {
        return Objects.requireNonNull(castGuiHolder(data.getTileEntity()), "Found TileEntity is not a gui holder!");
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, SidedPosGuiData guiData) {
        return player == guiData.getPlayer() && guiData.getTileEntity() != null && guiData.getSquaredDistance(player) <= 64;
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
