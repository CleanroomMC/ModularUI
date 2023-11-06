package com.cleanroommc.modularui.factory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PosGuiData extends GuiData {

    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    private final int x, y, z;

    public PosGuiData(EntityPlayer player, int x, int y, int z) {
        super(player);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public World getWorld() {
        return getPlayer().world;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public BlockPos getBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public TileEntity getTileEntity() {
        pos.setPos(this.x, this.y, this.z);
        return getWorld().getTileEntity(pos);
    }
}
