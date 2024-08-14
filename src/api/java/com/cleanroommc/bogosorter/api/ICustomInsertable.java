package com.cleanroommc.bogosorter.api;

import net.minecraft.inventory.Container;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ICustomInsertable {

    ItemStack insert(Container container, List<ISlot> slots, ItemStack itemStack, boolean emptyOnly);
}
