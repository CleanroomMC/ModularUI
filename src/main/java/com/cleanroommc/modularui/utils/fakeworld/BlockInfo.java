package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BlockInfo represents immutable information for block in world
 * This includes block state and tile entity, and needed for complete representation
 * of some complex blocks like machines, when rendering or manipulating them without world instance
 */
public class BlockInfo {

    public static final BlockInfo EMPTY = new BlockInfo(Blocks.AIR);
    public static final BlockInfo INVALID = new BlockInfo(Blocks.AIR);

    public static BlockInfo of(IBlockAccess world, BlockPos pos) {
        IBlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock().isAir(blockState, world, pos)) {
            return EMPTY;
        }
        TileEntity tile = null;
        if (blockState.getBlock().hasTileEntity(blockState)) {
            tile = world.getTileEntity(pos);
        }
        return new BlockInfo(blockState, tile);
    }

    private IBlockState blockState;
    private TileEntity tileEntity;

    public BlockInfo(@NotNull Block block) {
        this(block.getDefaultState());
    }

    public BlockInfo(@NotNull IBlockState blockState) {
        this(blockState, null);
    }

    public BlockInfo(@NotNull IBlockState blockState, @Nullable TileEntity tileEntity) {
        set(blockState, tileEntity);
    }

    public IBlockState getBlockState() {
        return blockState;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public void apply(World world, BlockPos pos) {
        world.setBlockState(pos, blockState);
        if (tileEntity != null) {
            world.setTileEntity(pos, tileEntity);
        } else {
            tileEntity = world.getTileEntity(pos);
        }
    }

    BlockInfo set(IBlockState state, TileEntity tile) {
        Preconditions.checkNotNull(state, "Block state must not be null!");
        Preconditions.checkArgument(tile == null || state.getBlock().hasTileEntity(state),
                "Cannot create block info with tile entity for block not having it!");
        this.blockState = state;
        this.tileEntity = tile;
        return this;
    }

    public boolean isMutable() {
        return false;
    }

    public Mut toMutable() {
        return new Mut(this.blockState, this.tileEntity);
    }

    public BlockInfo toImmutable() {
        return this;
    }

    public BlockInfo copy() {
        return new BlockInfo(this.blockState, this.tileEntity);
    }

    public static class Mut extends BlockInfo {

        public static final Mut SHARED = new Mut();

        public Mut() {
            this(Blocks.AIR);
        }

        public Mut(@NotNull Block block) {
            super(block);
        }

        public Mut(@NotNull IBlockState blockState) {
            super(blockState);
        }

        public Mut(@NotNull IBlockState blockState, @Nullable TileEntity tileEntity) {
            super(blockState, tileEntity);
        }

        @Override
        public Mut set(IBlockState state, TileEntity tile) {
            return (Mut) super.set(state, tile);
        }

        public Mut set(IBlockAccess world, BlockPos pos) {
            IBlockState blockState = world.getBlockState(pos);
            TileEntity tile = null;
            if (blockState.getBlock().hasTileEntity(blockState)) {
                tile = world.getTileEntity(pos);
            }
            return set(blockState, tile);
        }

        @Override
        public boolean isMutable() {
            return true;
        }

        @Override
        public Mut toMutable() {
            return this;
        }

        @Override
        public BlockInfo toImmutable() {
            return new BlockInfo(getBlockState(), getTileEntity());
        }

        @Override
        public Mut copy() {
            return new Mut(getBlockState(), getTileEntity());
        }
    }
}
