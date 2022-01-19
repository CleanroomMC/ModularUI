package io.github.cleanroommc.modularui.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class BaseSlot extends SlotItemHandler {

	protected final boolean output;
	protected final boolean phantom;

	public BaseSlot(IItemHandler inventory, boolean output, boolean phantom, int index, int xPos, int yPos) {
		super(inventory, index, xPos, yPos);
		this.output = output;
		this.phantom = phantom;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return !this.output;
	}

}
