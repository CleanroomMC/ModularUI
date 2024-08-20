package com.cleanroommc.modularui.utils;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class FluidTankHandler implements IFluidHandler {

    public static IFluidHandler getTankFluidHandler(IFluidTank tank) {
        if (tank instanceof IFluidHandler fluidHandler) {
            return fluidHandler;
        }
        return new FluidTankHandler(tank);
    }

    private final IFluidTank fluidTank;

    public FluidTankHandler(IFluidTank tank) {
        this.fluidTank = tank;
    }

    /*@Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[]{
                new FluidTank(this.fluidTank.getFluid(), this.fluidTank.getCapacity())
        };
    }*/

    @Override
    public int getTanks() {
        return 0;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        return null;
    }

    @Override
    public int getTankCapacity(int i) {
        return 0;
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return this.fluidTank.fill(resource, action);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack currentFluid = this.fluidTank.getFluid();
        if (currentFluid == null || currentFluid.getAmount() <= 0 || !currentFluid.isFluidEqual(resource)) {
            return null;
        }
        return this.fluidTank.drain(resource.getAmount(), action);
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return this.fluidTank.drain(maxDrain, action);
    }
}
