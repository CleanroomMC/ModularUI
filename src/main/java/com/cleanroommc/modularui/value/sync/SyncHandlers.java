package com.cleanroommc.modularui.value.sync;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.*;

public class SyncHandlers {

    private SyncHandlers() {
    }

    public static IntSyncValue intNumber(IntSupplier getter, IntConsumer setter) {
        return new IntSyncValue(getter, setter);
    }

    public static LongSyncValue longNumber(LongSupplier getter, LongConsumer setter) {
        return new LongSyncValue(getter, setter);
    }

    public static BooleanSyncValue bool(BooleanSupplier getter, Consumer<Boolean> setter) {
        return new BooleanSyncValue(getter, setter);
    }

    public static DoubleSyncValue doubleNumber(DoubleSupplier getter, DoubleConsumer setter) {
        return new DoubleSyncValue(getter, setter);
    }

    public static StringSyncValue string(Supplier<String> getter, Consumer<String> setter) {
        return new StringSyncValue(getter, setter);
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

    public static ItemSlotSH phantomItemSlot(IItemHandlerModifiable inventory, int index) {
        return ItemSlotSH.phantom(inventory, index);
    }

    public static FluidSlotSyncHandler fluidSlot(IFluidTank fluidTank) {
        return new FluidSlotSyncHandler(fluidTank);
    }

    public static <T extends Enum<T>> EnumSyncValue<T> enumValue(Class<T> clazz, Supplier<T> getter, Consumer<T> setter) {
        return new EnumSyncValue<>(clazz, getter, setter);
    }
}
