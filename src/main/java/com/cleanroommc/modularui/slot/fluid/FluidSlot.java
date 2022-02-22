package com.cleanroommc.modularui.slot.fluid;

import com.cleanroommc.modularui.slot.BaseSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;

public class FluidSlot extends BaseSlot {

	public FluidSlot(IItemHandler inventory, int index, boolean isOutput, boolean phantom) {
		super(inventory, index, isOutput, phantom);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
		return fluidHandler != null;
	}

}
