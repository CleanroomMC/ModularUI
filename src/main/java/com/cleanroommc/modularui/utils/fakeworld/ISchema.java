package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

public interface ISchema extends Iterable<Pair<BlockPos, BlockInfo>> {

    World getWorld();

    Vec3d getFocus();

    BlockPos getOrigin();

    void setRenderFilter(@Nullable BiPredicate<BlockPos, BlockInfo> renderFilter);

    @Nullable BiPredicate<BlockPos, BlockInfo> getRenderFilter();
}
