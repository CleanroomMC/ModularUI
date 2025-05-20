package com.cleanroommc.modularui.factory.inventory;

import net.minecraft.item.ItemStack;

public interface InventoryVisitor {

    boolean visit(InventoryType type, int index, ItemStack stack);
}
