package com.cleanroommc.modularui.slot.fluid;

import com.google.common.base.Predicates;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Predicate;

public class FilteredFluidSlot extends FluidSlot {

	protected final Predicate<FluidStack> fluidValidity;
	protected final Predicate<ItemStack> itemValidity;

	public FilteredFluidSlot(IItemHandler inventory, Predicate<FluidStack> fluidValidity, Predicate<ItemStack> itemValidity, boolean phantom, int index, int xPos, int yPos) {
		super(inventory, false, phantom, index, xPos, yPos);
		this.fluidValidity = fluidValidity;
		this.itemValidity = itemValidity;
	}

	public FilteredFluidSlot(IItemHandler inventory, Predicate<FluidStack> fluidValidity, boolean phantom, int index, int xPos, int yPos) {
		this(inventory, fluidValidity, Predicates.alwaysTrue(), phantom, index, xPos, yPos);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if (itemValidity.test(stack)) {
			IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
			if (fluidHandler != null) {
				return fluidValidity.test(fluidHandler.drain(Integer.MAX_VALUE, false));
			}
		}
		return false;
	}

}
