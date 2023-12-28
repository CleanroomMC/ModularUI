package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public interface ISchema extends Iterable<Map.Entry<BlockPos, BlockInfo>> {

    World getWorld();

    Vec3d getFocus();

    BlockPos getOrigin();
}
