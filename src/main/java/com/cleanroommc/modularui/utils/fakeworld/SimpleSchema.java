package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class SimpleSchema implements IMemorizingFilteredSchema {

    private final World world;
    private final Object2ObjectOpenHashMap<BlockPos, BlockInfo> originalBlocks = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<BlockPos, BlockInfo> filteredBlocks = new Object2ObjectOpenHashMap<>();
    private BiPredicate<BlockPos, BlockInfo> renderFilter;
    private final BlockPos origin;
    private final Vec3d center;

    public SimpleSchema(Map<BlockPos, BlockInfo> blocks) {
        this(blocks, (blockPos, blockInfo) -> true);
    }

    public SimpleSchema(Map<BlockPos, BlockInfo> blocks, BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.world = new DummyWorld();
        this.renderFilter = renderFilter;
        BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos(BlockPosUtil.MAX);
        BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos(BlockPosUtil.MIN);
        if (!blocks.isEmpty()) {
            for (var entry : blocks.entrySet()) {
                if (entry.getValue().getBlockState().getBlock() != Blocks.AIR) {
                    this.originalBlocks.put(entry.getKey(), entry.getValue());
                    entry.getValue().apply(this.world, entry.getKey());
                    BlockPosUtil.setMin(min, entry.getKey());
                    BlockPosUtil.setMax(max, entry.getKey());
                }
            }
        } else {
            min.setPos(0, 0, 0);
            max.setPos(0, 0, 0);
        }
        this.origin = min.toImmutable();
        this.center = BlockPosUtil.getCenterD(min, max);
        applyRenderFilter();
    }

    @Override
    public Map<BlockPos, BlockInfo> getOriginalSchema() {
        return Collections.unmodifiableMap(originalBlocks);
    }

    @Override
    public void applyRenderFilter() {
        filteredBlocks.clear();
        originalBlocks.forEach((pos, bInfo) -> {
            if (getRenderFilter().test(pos, bInfo)) {
                bInfo.apply(getWorld(), pos);
                filteredBlocks.put(pos, bInfo);
            } else getWorld().setBlockToAir(pos);
        });
    }

    @Override
    public void setRenderFilter(@NotNull BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.renderFilter = renderFilter;
    }

    @Override
    public @NotNull BiPredicate<BlockPos, BlockInfo> getRenderFilter() {
        return renderFilter;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public Vec3d getFocus() {
        return center;
    }

    @Override
    public BlockPos getOrigin() {
        return origin;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockInfo>> iterator() {
        return new Iterator<>() {

            private final ObjectIterator<Object2ObjectMap.Entry<BlockPos, BlockInfo>> it = filteredBlocks.object2ObjectEntrySet().fastIterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Map.Entry<BlockPos, BlockInfo> next() {
                return it.next();
            }
        };
    }

    public static class Builder {

        private final Object2ObjectOpenHashMap<BlockPos, BlockInfo> blocks = new Object2ObjectOpenHashMap<>();
        private BiPredicate<BlockPos, BlockInfo> renderFilter;

        public Builder add(BlockPos pos, IBlockState state) {
            return add(pos, state, null);
        }

        public Builder add(BlockPos pos, IBlockState state, TileEntity customTile) {
            if (state.getBlock() == Blocks.AIR) return this;
            this.blocks.put(pos, new BlockInfo(state, customTile));
            return this;
        }

        public Builder add(BlockPos pos, BlockInfo blockInfo) {
            this.blocks.put(pos, blockInfo.toImmutable());
            return this;
        }

        public Builder add(Iterable<BlockPos> posList, Function<BlockPos, BlockInfo> function) {
            for (BlockPos pos : posList) {
                BlockInfo info = function.apply(pos).toImmutable();
                add(pos, info);
            }
            return this;
        }

        public Builder add(Map<BlockPos, BlockInfo> blocks) {
            this.blocks.putAll(blocks);
            return this;
        }

        public Builder setRenderFilter(BiPredicate<BlockPos, BlockInfo> renderFilter) {
            this.renderFilter = renderFilter;
            return this;
        }

        public SimpleSchema build() {
            if (renderFilter == null) {
                return new SimpleSchema(this.blocks);
            }
            return new SimpleSchema(this.blocks, renderFilter);
        }
    }
}
