package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;

public abstract class PosListSchema implements IFilteredSchema {

    private final World world;
    private final Iterable<? extends BlockPos> posList;
    private BiPredicate<BlockPos, BlockInfo> renderFilter;

    public PosListSchema(World world, Iterable<? extends BlockPos> posList, BiPredicate<BlockPos, BlockInfo> renderFilter) {
        this.world = world;
        this.posList = posList;
        this.renderFilter = renderFilter;
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
        return world;
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<BlockPos, BlockInfo>> iterator() {
        return new Iterator<>() {

            private final Iterator<? extends BlockPos> posIt = PosListSchema.this.posList.iterator();
            private final MutablePair<BlockPos, BlockInfo> pair = new MutablePair<>();

            @Override
            public boolean hasNext() {
                return posIt.hasNext();
            }

            @Override
            public Pair<BlockPos, BlockInfo> next() {
                BlockPos pos = posIt.next();
                pair.setLeft(pos);
                BlockInfo.Mut.SHARED.set(PosListSchema.this.world, pos);
                if (renderFilter.test(pos, BlockInfo.Mut.SHARED)) {
                    pair.setRight(BlockInfo.Mut.SHARED);
                } else pair.setRight(BlockInfo.EMPTY);
                return pair;
            }
        };
    }
}
