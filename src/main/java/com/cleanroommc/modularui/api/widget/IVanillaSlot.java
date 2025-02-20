package com.cleanroommc.modularui.api.widget;

import net.minecraft.inventory.Slot;

/**
 * Marks a {@link IWidget}, that this is a vanilla item slot.
 */
public interface IVanillaSlot {

    /**
     * @return the item slot of this widget
     */
    Slot getVanillaSlot();

    boolean handleAsVanillaSlot();
}
