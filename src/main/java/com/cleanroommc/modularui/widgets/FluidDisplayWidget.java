package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.value.ObjectValue;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class FluidDisplayWidget extends AbstractFluidDisplayWidget<FluidDisplayWidget> {

    private IValue<FluidStack> value;
    private int capacity = 0;
    private boolean displayAmount = true;

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isValueOfType(FluidStack.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.value = syncOrValue.castValueNullable(FluidStack.class);
    }

    @Override
    protected boolean displayAmountText() {
        return this.displayAmount;
    }

    @Override
    protected @Nullable FluidStack getFluidStack() {
        return this.value != null ? this.value.getValue() : null;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    public FluidDisplayWidget value(IValue<FluidStack> value) {
        setSyncOrValue(value);
        return this;
    }

    public FluidDisplayWidget value(FluidStack value) {
        return value(new ObjectValue<>(FluidStack.class, value));
    }

    /**
     * Sets the capacity of the slot. This is only used for drawing and doesn't affect the actual capacity in any way. When the capacity is
     * greater than zero, the fluid will be drawn partially depending on the fill level.
     *
     * @param capacity capacity for drawing the fluid
     * @return this
     */
    public FluidDisplayWidget capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * Sets whether the amount number should be displayed in the bottom left corner of the slot.
     *
     * @param displayAmount true if amount should be displayed
     * @return this
     */
    public FluidDisplayWidget displayAmount(boolean displayAmount) {
        this.displayAmount = displayAmount;
        return this;
    }

    /**
     * Adds additional tooltip lines for the fluid. The function is called every frame.
     *
     * @param tooltip tooltip function for additional lines
     * @return this
     */
    public FluidDisplayWidget fluidTooltip(BiConsumer<RichTooltip, FluidStack> tooltip) {
        return tooltipAutoUpdate(true).tooltipBuilder(t -> tooltip.accept(t, getFluidStack()));
    }
}
