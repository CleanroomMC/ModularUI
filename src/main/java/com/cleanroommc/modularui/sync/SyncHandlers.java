package com.cleanroommc.modularui.sync;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.*;

public class SyncHandlers {

    private SyncHandlers() {
    }

    public static IntSyncHandler intNumber(IntSupplier getter, IntConsumer setter) {
        return new IntSyncHandler(getter, setter);
    }

    public static LongSyncHandler longNumber(LongSupplier getter, LongConsumer setter) {
        return new LongSyncHandler(getter, setter);
    }

    public static BooleanSyncHandler bool(BooleanSupplier getter, Consumer<Boolean> setter) {
        return new BooleanSyncHandler(getter, setter);
    }

    public static DoubleSyncHandler doubleNumber(DoubleSupplier getter, DoubleConsumer setter) {
        return new DoubleSyncHandler(getter, setter);
    }

    public static StringSyncHandler string(Supplier<String> getter, Consumer<String> setter) {
        return new StringSyncHandler(getter, setter);
    }

    public static ItemSlotSH itemSlot(Slot slot) {
        return new ItemSlotSH(slot);
    }

    public static ItemSlotSH itemSlot(IItemHandlerModifiable inventory, int index) {
        return new ItemSlotSH(inventory, index);
    }

    public static ItemSlotSH itemSlot(IInventory inventory, int index) {
        return new ItemSlotSH(inventory, index);
    }

    public static FluidSlotSyncHandler fluidSlot(IFluidTank fluidTank) {
        return new FluidSlotSyncHandler(fluidTank);
    }
}
