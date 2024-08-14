package com.cleanroommc.modularui.api.widget;

import net.minecraft.world.inventory.Slot;

/**
 * Marks a {@link IWidget}, that this is a vanilla item slot.
 */
@FunctionalInterface
public interface IVanillaSlot {

    /**
     * @return the item slot of this widget
     */
    Slot getVanillaSlot();
}
