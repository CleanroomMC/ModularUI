package com.cleanroommc.modularui.widgets.slot;

import net.minecraft.item.ItemStack;

public interface IOnSlotChanged {

    IOnSlotChanged DEFAULT = (newItem, onlyAmountChanged, client) -> {};

    void onChange(ItemStack newItem, boolean onlyAmountChanged, boolean client);
}
