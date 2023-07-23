package com.cleanroommc.modularui.manager;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

public class GuiCreationContext {

    private final EntityPlayer player;
    private final World world;
    private final int x, y, z;
    private final EnumHand hand;

    public GuiCreationContext(EntityPlayer player, World world, int x, int y, int z, EnumHand hand) {
        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.hand = hand;
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public World getWorld() {
        return this.world;
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

    public EnumHand getUsedHand() {
        return this.hand;
    }

    public ItemStack getMainHandItem() {
        return this.player.getHeldItemMainhand();
    }

    public ItemStack getOffHandItem() {
        return this.player.getHeldItemOffhand();
    }

    public ItemStack getUsedItemStack() {
        return this.player.getHeldItem(this.hand);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public IBlockState getBlockState() {
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(this.x, this.y, this.z);
        IBlockState blockState = this.world.getBlockState(pos);
        pos.release();
        return blockState;
    }

    public TileEntity getTileEntity() {
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain(this.x, this.y, this.z);
        TileEntity tile = this.world.getTileEntity(pos);
        pos.release();
        return tile;
    }

    @ApiStatus.Internal
    public GuiCreationContext with(EnumHand hand) {
        if (this.hand != hand) {
            return new GuiCreationContext(this.player, this.world, this.x, this.y, this.z, hand);
        }
        return this;
    }
}
