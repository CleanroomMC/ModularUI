package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.Objects;

public class TileEntityGuiFactory extends AbstractUIFactory<PosGuiData> {

    public static final TileEntityGuiFactory INSTANCE = new TileEntityGuiFactory();

    private TileEntityGuiFactory() {
        super("mui:tile");
    }

    public <T extends TileEntity & IGuiHolder<PosGuiData>> void open(EntityPlayer player, T tile) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(tile);
        if (tile.isInvalid()) {
            throw new IllegalArgumentException("Can't open invalid TileEntity GUI!");
        }
        if (player.world != tile.getWorld()) {
            throw new IllegalArgumentException("TileEntity must be in same dimension as the player!");
        }
        BlockPos pos = tile.getPos();
        PosGuiData data = new PosGuiData(player, pos.getX(), pos.getY(), pos.getZ());
        GuiManager.open(this, data, (EntityPlayerMP) player);
    }

    @Override
    public ModularPanel createPanel(PosGuiData guiData, GuiSyncManager syncManager) {
        TileEntity te = guiData.getTileEntity();
        if (!(te instanceof IGuiHolder)) throw new NoSuchElementException();
        IGuiHolder<PosGuiData> guiHolder = (IGuiHolder<PosGuiData>) te;
        return guiHolder.buildUI(guiData, syncManager, guiData.getWorld().isRemote);
    }

    @Override
    public ModularScreen createScreen(PosGuiData guiData, ModularPanel mainPanel) {
        TileEntity te = guiData.getTileEntity();
        if (!(te instanceof IGuiHolder)) throw new NoSuchElementException();
        IGuiHolder<PosGuiData> guiHolder = (IGuiHolder<PosGuiData>) te;
        return guiHolder.createScreen(guiData, mainPanel);
    }

    @Override
    public void writeGuiData(PosGuiData guiData, PacketBuffer buffer) {
        buffer.writeVarInt(guiData.getX());
        buffer.writeVarInt(guiData.getY());
        buffer.writeVarInt(guiData.getZ());
    }

    @Override
    public @NotNull PosGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new PosGuiData(player, buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }
}
