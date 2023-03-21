package com.cleanroommc.modularui.manager;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiCreationContext {

    private final EntityPlayer player;
    private final World world;
    private final int x, y, z;

    public GuiCreationContext(EntityPlayer player, World world, int x, int y, int z) {
        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public ItemStack getMainHandItem() {
        return player.getHeldItemMainhand();
    }

    public ItemStack getOffHandItem() {
        return player.getHeldItemOffhand();
    }

    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }

    public IBlockState getBlockState() {
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(x, y, z);
        IBlockState blockState = world.getBlockState(pos);
        pos.release();
        return blockState;
    }

    public TileEntity getTileEntity() {
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(x, y, z);
        TileEntity tile = world.getTileEntity(pos);
        pos.release();
        return tile;
    }
}
