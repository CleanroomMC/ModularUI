package com.cleanroommc.modularui.utils.fakeworld;

import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;

/**
 * This schema gets filtered on insertion. <p>
 * If the filter changes, it will need to manually call {@link IMemorizingFilteredSchema#applyRenderFilter()}
 */
public interface IMemorizingFilteredSchema extends IFilteredSchema {

    @Unmodifiable Map<BlockPos, BlockInfo> getOriginalSchema();
    void applyRenderFilter();
}
