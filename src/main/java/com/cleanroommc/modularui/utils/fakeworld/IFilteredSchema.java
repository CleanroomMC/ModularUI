package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

/**
 * This schema gets filtered on insertion. <p>
 * If the filter changes, it will not get applied to ealready set blocks
 */
public interface IFilteredSchema extends ISchema {

    void setRenderFilter(@NotNull BiPredicate<BlockPos, BlockInfo> renderFilter);

    @NotNull BiPredicate<BlockPos, BlockInfo> getRenderFilter();

}
