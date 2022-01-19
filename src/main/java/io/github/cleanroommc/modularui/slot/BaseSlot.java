package io.github.cleanroommc.modularui.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BaseSlot extends Slot {

	protected final boolean output;
	protected final boolean phantom;

	public BaseSlot(IInventory inventory, boolean output, boolean phantom, int index, int xPos, int yPos) {
		super(inventory, index, xPos, yPos);
		this.output = output;
		this.phantom = phantom;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return !this.output;
	}

}
