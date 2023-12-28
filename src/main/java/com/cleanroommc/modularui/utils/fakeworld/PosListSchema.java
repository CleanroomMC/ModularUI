package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

public abstract class PosListSchema implements ISchema {

    private final World world;
    private final Iterable<? extends BlockPos> posList;

    public PosListSchema(World world, Iterable<? extends BlockPos> posList) {
        this.world = world;
        this.posList = posList;
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
                pair.setRight(BlockInfo.Mut.SHARED.set(PosListSchema.this.world, pos));
                return pair;
            }
        };
    }
}
