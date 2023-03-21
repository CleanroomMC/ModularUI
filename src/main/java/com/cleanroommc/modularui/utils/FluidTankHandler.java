package com.cleanroommc.modularui.utils;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class FluidTankHandler implements IFluidHandler {

    public static IFluidHandler getTankFluidHandler(IFluidTank tank) {
        if (tank instanceof IFluidHandler) {
            return (IFluidHandler) tank;
        }
        return new FluidTankHandler(tank);
    }

    private final IFluidTank fluidTank;

    public FluidTankHandler(IFluidTank tank) {
        this.fluidTank = tank;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[]{
                new FluidTankProperties(fluidTank.getFluid(), fluidTank.getCapacity())
        };
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return fluidTank.fill(resource, doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        FluidStack currentFluid = fluidTank.getFluid();
        if (currentFluid == null || currentFluid.amount <= 0 || !currentFluid.isFluidEqual(resource)) {
            return null;
        }
        return fluidTank.drain(resource.amount, doDrain);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return fluidTank.drain(maxDrain, doDrain);
    }
}
