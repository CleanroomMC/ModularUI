package com.cleanroommc.bogosorter.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * implement on {@link net.minecraft.inventory.Container}
 */
public interface ISortableContainer {

    @ApiStatus.OverrideOnly
    void buildSortingContext(ISortingContextBuilder builder);
}
