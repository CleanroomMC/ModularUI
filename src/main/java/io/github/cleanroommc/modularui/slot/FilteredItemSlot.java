package io.github.cleanroommc.modularui.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class FilteredItemSlot extends BaseSlot {

	protected final Predicate<ItemStack> checkItemValidity;

	public FilteredItemSlot(IInventory inventory, boolean phantom, Predicate<ItemStack> checkItemValidity, int index, int xPos, int yPos) {
		super(inventory, false, phantom, index, xPos, yPos);
		this.checkItemValidity = checkItemValidity;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return checkItemValidity.test(stack);
	}

}
