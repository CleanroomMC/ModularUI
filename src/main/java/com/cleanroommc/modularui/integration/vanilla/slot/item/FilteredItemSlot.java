package com.cleanroommc.modularui.integration.vanilla.slot.item;

import com.cleanroommc.modularui.integration.vanilla.slot.BaseSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Predicate;

public class FilteredItemSlot extends BaseSlot {

	protected final Predicate<ItemStack> checkItemValidity;

	public FilteredItemSlot(IItemHandler inventory, boolean phantom, Predicate<ItemStack> checkItemValidity, int index) {
		super(inventory, index, false, phantom);
		this.checkItemValidity = checkItemValidity;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return checkItemValidity.test(stack);
	}

}
