package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiPredicate;

public interface ISchema extends Iterable<Map.Entry<BlockPos, BlockInfo>> {

    World getWorld();

    Vec3d getFocus();

    BlockPos getOrigin();

    void setRenderFilter(@Nullable BiPredicate<BlockPos, BlockInfo> renderFilter);

    @Nullable BiPredicate<BlockPos, BlockInfo> getRenderFilter();
}
