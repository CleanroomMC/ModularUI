package io.github.cleanroommc.modularui.slot.fluid;

import io.github.cleanroommc.modularui.slot.BaseSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;

public class FluidSlot extends BaseSlot {

	public FluidSlot(IItemHandler inventory, boolean isOutput, boolean phantom, int index, int xPos, int yPos) {
		super(inventory, isOutput, phantom, index, xPos, yPos);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
		return fluidHandler != null;
	}

}
