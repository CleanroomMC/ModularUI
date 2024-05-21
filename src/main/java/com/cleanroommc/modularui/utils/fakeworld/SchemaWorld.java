package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;

public class SchemaWorld extends DummyWorld implements ISchema{

    private final ObjectLinkedOpenHashSet<BlockPos> blocks = new ObjectLinkedOpenHashSet<>();
    private BiPredicate<BlockPos, BlockInfo> renderFilter;
    private final BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos();

    public SchemaWorld() {
        this((blockPos, blockInfo) -> true);
    }

    public SchemaWorld(BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.renderFilter = renderFilter;
    }

    @Override
    public void setRenderFilter(@Nullable BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.renderFilter = renderFilter;
    }

    @Override
    public @Nullable BiPredicate<BlockPos, BlockInfo> getRenderFilter() {
        return renderFilter;
    }

    @Override
    public boolean setBlockState(@NotNull BlockPos pos, @NotNull IBlockState newState, int flags) {
        boolean renderTest;
        boolean state;
        if (renderFilter == null || renderFilter.test(pos, BlockInfo.of(this, pos))) {
            renderTest = true;
            state = super.setBlockState(pos, newState, flags);
        } else {
            renderTest = state = false;
        }

        if (newState.getBlock().isAir(newState, this, pos)) {
            if (this.blocks.remove(pos) && BlockPosUtil.isOnBorder(min, max, pos)) {
                if (this.blocks.isEmpty()) {
                    this.min.setPos(0, 0, 0);
                    this.max.setPos(0, 0, 0);
                } else {
                    min.setPos(BlockPosUtil.MAX);
                    max.setPos(BlockPosUtil.MIN);
                    for (BlockPos pos1 : blocks) {
                        BlockPosUtil.setMin(min, pos1);
                        BlockPosUtil.setMax(max, pos1);
                    }
                }
            }
        } else if (this.blocks.isEmpty()) {
            if (!renderTest) return false;
            this.blocks.add(pos);
            this.min.setPos(pos);
            this.max.setPos(pos);
        } else if (renderTest && this.blocks.add(pos)) {
            BlockPosUtil.setMin(this.min, pos);
            BlockPosUtil.setMax(this.max, pos);
        }
        return renderTest && state;
    }

    @Override
    public World getWorld() {
        return this;
    }

    @Override
    public Vec3d getFocus() {
        return BlockPosUtil.getCenterD(this.min, this.max);
    }

    @Override
    public BlockPos getOrigin() {
        return this.min;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockInfo>> iterator() {
        return new Iterator<>() {

            private final ObjectIterator<BlockPos> it = blocks.iterator();
            private final BlockInfo.Mut info = new BlockInfo.Mut();
            private final MutablePair<BlockPos, BlockInfo> pair = new MutablePair<>(null, this.info);

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Map.Entry<BlockPos, BlockInfo> next() {
                BlockPos pos = it.next();
                this.info.set(SchemaWorld.this, pos);
                this.pair.setLeft(pos);
                return this.pair;
            }
        };
    }
}
