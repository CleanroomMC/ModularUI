package com.cleanroommc.modularui.sync;

import com.cleanroommc.modularui.api.ValueSyncHandler;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.utils.FluidTankHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FluidSlotSyncHandler extends ValueSyncHandler<FluidStack> {

    private FluidStack cache;
    private final IFluidTank fluidTank;
    private final IFluidHandler fluidHandler;

    public FluidSlotSyncHandler(IFluidTank fluidTank) {
        this.fluidTank = fluidTank;
        this.fluidHandler = FluidTankHandler.getTankFluidHandler(fluidTank);
    }

    @Nullable
    @Override
    public FluidStack getCachedValue() {
        return cache;
    }

    @Override
    public void setValue(@Nullable FluidStack value) {
        this.cache = value;
        this.fluidTank.drain(Integer.MAX_VALUE, true);
        this.fluidTank.fill(value, true);
    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        if (isFirstSync) return true;
        FluidStack current = this.fluidTank.getFluid();
        if (current == cache) return false;
        if (current == null || cache == null) return true;
        return current.amount != cache.amount || !current.isFluidEqual(cache);
    }

    @Override
    public void write(PacketBuffer buffer) {
        NetworkUtils.writeFluidStack(buffer, cache);
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(NetworkUtils.readFluidStack(buffer));
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 0) {
            read(buf);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 0) {
            read(buf);
        }
    }
}
