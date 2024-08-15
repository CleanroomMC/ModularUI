package com.cleanroommc.modularui.factory;

import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PosGuiData extends GuiData {

    private static final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    private final int x, y, z;

    public PosGuiData(Player player, int x, int y, int z) {
        super(player);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Level getWorld() {
        return getPlayer().level();
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

    public BlockEntity getTileEntity() {
        pos.set(this.x, this.y, this.z);
        return getWorld().getBlockEntity(pos);
    }
}
