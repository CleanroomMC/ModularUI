package io.github.cleanroommc.modularui.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidSlot extends BaseSlot {

	public FluidSlot(IInventory inventory, boolean isOutput, boolean phantom, int index, int xPos, int yPos) {
		super(inventory, isOutput, phantom, index, xPos, yPos);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
		return fluidHandler != null;
	}

}
