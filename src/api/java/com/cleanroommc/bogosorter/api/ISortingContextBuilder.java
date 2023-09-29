package com.cleanroommc.bogosorter.api;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import java.util.List;

/**
 * A helper interface to create {@link ISlotGroup} instances.
 * Meant to be used in {@link ISortableContainer#buildSortingContext(ISortingContextBuilder)}
 */
public interface ISortingContextBuilder {

    /**
     * Creates and registers a slot group with a list of slots.
     *
     * @param slots   slot list
     * @param rowSize This is mostly used to determine the button position with {@link IPosSetter#TOP_RIGHT_VERTICAL}
     *                and {@link IPosSetter#TOP_RIGHT_HORIZONTAL}. If the slot group shape is not rectangular,
     *                try to use the row size of the first row.
     * @return the created slot group
     */
    ISlotGroup addSlotGroupOf(List<Slot> slots, int rowSize);

    /**
     * Creates and registers a slot group with a list of slots.
     *
     * @param slots   slot list
     * @param rowSize This is mostly used to determine the button position with {@link IPosSetter#TOP_RIGHT_VERTICAL}
     *                and {@link IPosSetter#TOP_RIGHT_HORIZONTAL}. If the slot group shape is not rectangular,
     *                try to use the row size of the first row.
     * @return the created slot group
     */
    ISlotGroup addSlotGroup(List<ISlot> slots, int rowSize);

    /**
     * Creates and registers a slot group based on a start and end index.
     *
     * @param startIndex index of the first slot (including)
     * @param endIndex   index of the end slot (excluding)
     * @param rowSize    This is mostly used to determine the button position with {@link IPosSetter#TOP_RIGHT_VERTICAL}
     *                   and {@link IPosSetter#TOP_RIGHT_HORIZONTAL}. If the slot group shape is not rectangular,
     *                   try to use the row size of the first row.
     * @return the created slot group
     */
    ISlotGroup addSlotGroup(int startIndex, int endIndex, int rowSize);

    Container getContainer();
}
