package com.cleanroommc.modularui.factory;

import com.cleanroommc.modularui.api.IDistantTile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * See {@link GuiData} for an explanation for what this is for.
 */
public class PosGuiData extends GuiData {

    private final BlockPos pos;
    private final TileEntity openedTile;
    private final double interactDistanceSq;

    public PosGuiData(EntityPlayer player, int x, int y, int z) {
        super(player);
        this.pos = new BlockPos(x, y, z);
        openedTile = getTileEntity();
        double interactDistance = openedTile instanceof IDistantTile ? ((IDistantTile) openedTile).getInteractionDistance(): 8;
        this.interactDistanceSq = interactDistance * interactDistance;
    }

    public World getWorld() {
        return getPlayer().world;
    }

    public int getX() {
        return this.pos.getX();
    }

    public int getY() {
        return this.pos.getY();
    }

    public int getZ() {
        return this.pos.getZ();
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public TileEntity getTileEntity() {
        return getWorld().getTileEntity(pos);
    }
    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return super.canInteractWith(playerIn) && playerIn.getDistanceSqToCenter(pos) < interactDistanceSq && getTileEntity() == openedTile;
    }
}
