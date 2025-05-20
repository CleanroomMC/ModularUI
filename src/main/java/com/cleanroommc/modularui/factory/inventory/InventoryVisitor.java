package com.cleanroommc.modularui.factory.inventory;

import net.minecraft.item.ItemStack;

/**
 * A function to visit a slot in a player bound inventory.
 */
public interface InventoryVisitor {

    /**
     * Called on visiting a slot in a player bound inventory.
     *
     * @param type type of the current inventory
     * @param index index of the slot
     * @param stack content of the slot
     * @return true if no further slots should be visited
     */
    boolean visit(InventoryType type, int index, ItemStack stack);
}
