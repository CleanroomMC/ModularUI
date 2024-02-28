package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

public class BoxSchema extends PosListSchema {

    public static BoxSchema of(World world, BlockPos center, int r) {
        return new BoxSchema(world, center.add(-r, -r, -r), center.add(r, r, r), (blockPos, blockInfo) -> true);
    }

    public static BoxSchema of(World world, BlockPos center, int r, BiPredicate<BlockPos, BlockInfo> renderFilter) {
        return new BoxSchema(world, center.add(-r, -r, -r), center.add(r, r, r), renderFilter);
    }

    private final World world;
    private final BlockPos min, max;
    private final Vec3d center;

    public BoxSchema(World world, BlockPos min, BlockPos max, BiPredicate<BlockPos, BlockInfo> renderFilter) {
        super(world, BlockPosUtil.getAllInside(min, max, false), renderFilter);
        this.world = world;
        this.min = BlockPosUtil.getMin(min, max);
        this.max = BlockPosUtil.getMax(min, max);
        this.center = BlockPosUtil.getCenterD(min, max);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Vec3d getFocus() {
        return center;
    }

    @Override
    public BlockPos getOrigin() {
        return min;
    }

    public BlockPos getMin() {
        return min;
    }

    public BlockPos getMax() {
        return max;
    }
}
