package com.cleanroommc.bogosorter.api;

import java.util.List;

/**
 * A slot group is a list of slots which is organized in a rectangle.
 * It doesn't necessarily need to be a rectangle, but you might run into issues if the shape is more complex.
 */
public interface ISlotGroup {

    /**
     * An unmodifiable view of all the slots of this group.
     *
     * @return all slots
     */
    List<ISlot> getSlots();

    /**
     * Returns how many slots are in row. This is mostly used to determine the button position with
     * {@link IPosSetter#TOP_RIGHT_VERTICAL} and {@link IPosSetter#TOP_RIGHT_HORIZONTAL}. If the slot group shape is
     * not rectangular, try to return the row size of the first row.
     *
     * @return slots per row
     */
    int getRowSize();

    /**
     * Returns the priority that this group takes when items are transferred via shortcuts.
     *
     * @return priority
     */
    int getPriority();

    /**
     * Returns if this slot groups only consists of player inventory slots. It does not need to be the full player
     * inventory. The player hotbar is usually not part of this.
     *
     * @return if all slots are part of the player inventory
     */
    boolean isPlayerInventory();

    /**
     * Sets the priority of this slot group. Can determine where items are transferred first with shortcuts.
     * Returns itself to be used in a builder like manner.
     *
     * @param priority priority
     * @return this
     */
    ISlotGroup priority(int priority);

    /**
     * Sets a custom function to determine the position of sort buttons. Default is top right corner.
     * Returns itself to be used in a builder like manner.
     *
     * @param posSetter pos function or null if no buttons are desired
     * @return this
     */
    ISlotGroup buttonPosSetter(IPosSetter posSetter);
}
